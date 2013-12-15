package com.darkrockstudios.views.uriimageview;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.darkrockstudios.apps.randdit.R;
import com.darkrockstudios.apps.randdit.misc.StatCounter;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import pl.droidsonroids.gif.GifDrawable;

/**
 * Created by Adam on 11/25/13.
 */
public class UriImageDownloadTask extends AsyncTask<Uri, Integer, Drawable>
{
	private static final String TAG = UriImageDownloadTask.class.getSimpleName();

	private static final String   GIF_MIME_TYPE     = "image/gif";
	private static final String   JPG_MIME_TYPE     = "image/jpeg";
	private static final String   PJPG_MIME_TYPE    = "image/pjpeg";
	private static final String   PNG_MIME_TYPE     = "image/png";
	private static final String   BMP_MIME_TYPE     = "image/bmp";
	private static final String   WEBP_MIME_TYPE    = "image/webp";
	private static final String[] SUPPORTED_FORMATS = {
			                                                  JPG_MIME_TYPE,
			                                                  PJPG_MIME_TYPE,
			                                                  PNG_MIME_TYPE,
			                                                  BMP_MIME_TYPE,
			                                                  GIF_MIME_TYPE,
			                                                  WEBP_MIME_TYPE
	};

	private static final String CONTENT_TYPE   = "Content-Type";
	private static final String CONTENT_LENGTH = "Content-Length";

	private final Context          m_context;
	private       UriImageHandler  m_imageHandler;
	private       int              m_errorImageId;
	private       LruDrawableCache m_cache;
	private final int              m_maxBitmapWidth;
	private final int              m_maxBitmapHeight;
	private       boolean          m_canceled;
	private       Uri              m_uri;

	public UriImageDownloadTask( final UriImageHandler imageHandler, final Uri uri, final Context context )
	{
		m_imageHandler = imageHandler;
		m_context = context;

		UriImageView imageView = m_imageHandler.getImageView();
		m_errorImageId = imageView.getErrorImageId();
		m_cache = imageView.getCache();
		m_maxBitmapWidth = imageView.getMaxBitmapWidth();
		m_maxBitmapHeight = imageView.getMaxBitmapHeight();

		m_uri = uri;
	}

	public void cancel()
	{
		Log.d( TAG, "Canceling download for URI: " + (m_uri != null ? m_uri : "null") );
		m_canceled = true;
	}

	public Uri getUri()
	{
		return m_uri;
	}

	@Override
	protected Drawable doInBackground( final Uri... params )
	{
		Drawable drawable = null;
		if( m_errorImageId != 0 )
		{
			drawable = m_context.getResources().getDrawable( R.drawable.image_error );
		}

		HttpURLConnection urlConnection = null;

		ByteArrayOutputStream imageStream = null;
		List<String> mimeTypes = null;

		InputStream inputStream = null;
		try
		{
			URL url = new URL( m_uri.toString() );
			urlConnection = (HttpURLConnection) url.openConnection();

			publishProgress( 1, 100 );
			Map<String, List<String>> headers = urlConnection.getHeaderFields();
			if( headers != null )
			{
				mimeTypes = headers.get( CONTENT_TYPE );
			}
			// We must try and guess the Mimetype from the URL if we didn't get headers
			else
			{
				ContentResolver contentResolver = m_context.getContentResolver();
				MimeTypeMap mime = MimeTypeMap.getSingleton();

				String type = contentResolver.getType( m_uri );
				if( type != null )
				{
					String mimeType = mime.getExtensionFromMimeType( type );
					if( mimeType != null )
					{
						mimeTypes = new ArrayList<>();
						mimeTypes.add( mimeType );
					}
				}
			}

			final int contentLength;
			if( headers != null )
			{
				List<String> contentLengths = headers.get( CONTENT_LENGTH );
				if( contentLengths != null && contentLengths.size() > 0 )
				{
					contentLength = Integer.parseInt( contentLengths.get( 0 ) );
				}
				else
				{
					contentLength = 0;
					publishProgress( 0, 0, -1 );
				}
			}
			else
			{
				contentLength = 0;
				publishProgress( 0, 0, -1 );
			}

			if( contentLength > 0 )
			{
				imageStream = new ByteArrayOutputStream( contentLength );
			}
			else
			{
				imageStream = new ByteArrayOutputStream();
			}
			inputStream = new BufferedInputStream( urlConnection.getInputStream() );

			final int BUF_SIZE = 1024;
			final byte[] buffer = new byte[ BUF_SIZE ];

			int bytesRead;
			do
			{
				bytesRead = inputStream.read( buffer );
				if( bytesRead > 0 )
				{
					imageStream.write( buffer, 0, bytesRead );

					if( contentLength > 0 )
					{
						publishProgress( bytesRead, contentLength );
					}
				}
			} while( bytesRead > 0 && !m_canceled );

			if( m_canceled )
			{
				Log.d( TAG,
				       "Image download canceled! " + imageStream.size() + "B of " + contentLength + "B had been downloaded" );
			}
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
		finally
		{
			if( urlConnection != null )
			{
				urlConnection.disconnect();
			}

			if( inputStream != null )
			{
				try
				{
					inputStream.close();
				}
				catch( IOException e )
				{
					e.printStackTrace();
				}
			}

			// If canceled, close our image stream
			if( m_canceled )
			{
				try
				{
					imageStream.close();
				}
				catch( IOException e )
				{
					e.printStackTrace();
				}
			}
		}

		if( !m_canceled )
		{
			Drawable downloadedDrawable = createDrawable( imageStream, mimeTypes );
			if( downloadedDrawable != null )
			{
				drawable = downloadedDrawable;

				// This doesn't really belong here, but it's the best place I can think to put it for now
				StatCounter.countImageView( m_context );
			}
		}

		return drawable;
	}

	private Drawable createDrawable( final ByteArrayOutputStream imageStream, final List<String> mimeTypes )
	{
		Drawable drawable = null;

		try
		{
			if( imageStream != null && imageStream.size() > 0 && !m_canceled )
			{
				byte[] bits = imageStream.toByteArray();

				if( checkForGif( bits ) || findMimeType( GIF_MIME_TYPE, mimeTypes ) )
				{
					try
					{
						GifDrawable gifDrawable = new GifDrawable( bits );
						if( m_cache != null )
						{
							m_cache.putDrawable( m_uri, gifDrawable );
						}
						drawable = gifDrawable;
					}
					catch( IOException e )
					{
						e.printStackTrace();
					}
				}
				else if( isSupportedFormat( mimeTypes ) )
				{
					Bitmap bitmap = BitmapFactory.decodeByteArray( bits, 0, bits.length );
					if( bitmap != null )
					{
						int width = bitmap.getWidth();
						int height = bitmap.getHeight();

						if( width > m_maxBitmapWidth || height > m_maxBitmapHeight )
						{
							Log.i( TAG, "Resizing image: " + width + "x" + height );

							final int widthDelta = width - m_maxBitmapWidth;
							final int heightDelta = height - m_maxBitmapHeight;

							final double scale;
							if( widthDelta > heightDelta )
							{
								scale = (double) m_maxBitmapWidth / (double) width;
							}
							else
							{
								scale = (double) m_maxBitmapHeight / (double) height;
							}

							width = (int) (width * scale);
							height = (int) (height * scale);

							Log.i( TAG, "New size: " + width + "x" + height );

							bitmap = Bitmap.createScaledBitmap( bitmap, width, height, false );
						}

						BitmapDrawable bitmapDrawable = new BitmapDrawable( m_context.getResources(), bitmap );
						if( m_cache != null )
						{
							m_cache.putDrawable( m_uri, bitmapDrawable );
						}
						drawable = bitmapDrawable;
					}
				}
			}
		}
		catch( OutOfMemoryError e )
		{
			Log.w( TAG, "Ran out of memory trying to decode image." );
			try
			{
				imageStream.close();
			}
			catch( IOException e1 )
			{
				e1.printStackTrace();
			}
		}

		return drawable;
	}

	private boolean checkForGif( final byte[] bytes )
	{
		boolean isGif;

		final byte[] GIF87a = { 0x47, 0x49, 0x46, 0x38, 0x37, 0x61 };
		final byte[] GIF89a = { 0x47, 0x49, 0x46, 0x38, 0x39, 0x61 };

		isGif = checkMagicWord( bytes, GIF87a );
		if( !isGif )
		{
			isGif = checkMagicWord( bytes, GIF89a );
		}

		return isGif;
	}

	private boolean checkMagicWord( final byte[] bytes, final byte[] magicWord )
	{
		boolean magicWordFound = false;

		if( bytes != null && magicWord != null && bytes.length > magicWord.length )
		{
			magicWordFound = true;
			for( int ii = 0; ii < magicWord.length; ++ii )
			{
				if( bytes[ ii ] != magicWord[ ii ] )
				{
					magicWordFound = false;
					break;
				}
			}
		}

		return magicWordFound;
	}

	@Override
	protected void onProgressUpdate( final Integer... progress )
	{
		if( !m_canceled )
		{
			m_imageHandler.onProgressUpdate( progress );
		}
	}

	@Override
	protected void onPostExecute( final Drawable drawable )
	{
		if( !m_canceled )
		{
			m_imageHandler.onPostExecute( drawable );
		}
	}

	private boolean isSupportedFormat( final List<String> mimeTypes )
	{
		boolean supported = false;

		if( mimeTypes != null )
		{
			for( String supportedMimeType : SUPPORTED_FORMATS )
			{
				if( findMimeType( supportedMimeType, mimeTypes ) )
				{
					supported = true;
					break;
				}
			}
		}
		// This is the end of the line. Everything has failed. We have no fucking idea what mime type this thing is.
		// The server didn't send headers, and we weren't able to deduce the type from the URL. Give up and throw
		// caution to the wind! Lets mark it as supported and try to decode this bitch!
		else
		{
			supported = true;
		}

		return supported;
	}

	private boolean findMimeType( final String type, final List<String> mimeTypes )
	{
		boolean typeFound = false;

		if( mimeTypes != null )
		{
			for( String mimeType : mimeTypes )
			{
				if( mimeType.equalsIgnoreCase( type ) )
				{
					typeFound = true;
					break;
				}
			}
		}

		return typeFound;
	}
}