package com.com.darkrockstudios.views.urlimageview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.darkrockstudios.apps.randdit.R;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import pl.droidsonroids.gif.GifDrawable;
import uk.co.senab.photoview.PhotoView;

/**
 * Created by Adam on 11/22/13.
 */
public class UrlImageView extends FrameLayout
{
	private static final String   TAG               = UrlImageView.class.getSimpleName();
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

	private LruDrawableCache m_cache;

	private Uri               m_uri;
	private ImageDownloadTask m_downloadTask;

	private PhotoView   m_imageView;
	private ProgressBar m_progressBar;

	private int m_errorImageId;

	private static final int DEFAULT_MAX_SIZE = 2048;
	private boolean m_maxDimensionsSet;
	private int m_maxBitmapWidth  = DEFAULT_MAX_SIZE;
	private int m_maxBitmapHeight = DEFAULT_MAX_SIZE;

	private void setupViews( final Context context, final AttributeSet attrs, final int defStyle )
	{
		m_maxDimensionsSet = false;

		m_imageView = new PhotoView( context );
		addView( m_imageView );

		m_progressBar = new ProgressBar( context, null, android.R.attr.progressBarStyleHorizontal );
		m_progressBar.setIndeterminate( false );
		m_progressBar.setVisibility( View.INVISIBLE );
		addView( m_progressBar );
	}

	public UrlImageView( final Context context, final AttributeSet attrs, final int defStyle )
	{
		super( context, attrs, defStyle );
		setupViews( context, attrs, defStyle );
	}

	public UrlImageView( final Context context, final AttributeSet attrs )
	{
		super( context, attrs );
		setupViews( context, attrs, 0 );
	}

	public UrlImageView( final Context context )
	{
		super( context );
		setupViews( context, null, 0 );
	}

	public void setErrorImage( final int errorImageId )
	{
		m_errorImageId = errorImageId;
	}

	@Override
	public void onDraw( final Canvas canvas )
	{
		super.onDraw( canvas );

		if( !m_maxDimensionsSet )
		{
			m_maxBitmapHeight = canvas.getMaximumBitmapHeight();
			m_maxBitmapWidth = canvas.getMaximumBitmapWidth();
			Log.i( TAG, "Got max dimensions: " + m_maxBitmapWidth + "x" + m_maxBitmapHeight );
			m_maxDimensionsSet = true;
		}
	}

	public void loadImage( final Uri uri, final LruDrawableCache cache )
	{
		m_cache = cache;

		if( uri != null )
		{
			if( !uri.equals( m_uri ) && m_downloadTask == null )
			{
				final Drawable drawable;
				if( m_cache != null )
				{
					drawable = m_cache.getDrawable( uri );
				}
				else
				{
					drawable = null;
				}

				if( drawable == null )
				{
					m_progressBar.setIndeterminate( false );
					m_progressBar.setProgress( 0 );
					m_progressBar.setVisibility( View.VISIBLE );

					m_uri = uri;
					m_downloadTask = new ImageDownloadTask();
					m_downloadTask.execute( m_uri );
				}
				else
				{
					m_imageView.setImageDrawable( drawable );
				}
			}
		}
		else
		{
			m_imageView.setImageDrawable( null );
		}
	}

	private class ImageDownloadTask extends AsyncTask<Uri, Integer, Drawable>
	{

		@Override
		protected Drawable doInBackground( final Uri... params )
		{
			Drawable drawable = null;
			if( m_errorImageId != 0 )
			{
				drawable = getResources().getDrawable( R.drawable.image_error );
			}

			Uri uri = params[ 0 ];
			HttpURLConnection urlConnection = null;

			ByteArrayOutputStream imageStream = null;
			List<String> mimeTypes = null;

			InputStream inputStream = null;
			try
			{
				URL url = new URL( uri.toString() );
				urlConnection = (HttpURLConnection) url.openConnection();

				publishProgress( 1, 100 );
				Map<String, List<String>> headers = urlConnection.getHeaderFields();
				mimeTypes = headers.get( CONTENT_TYPE );

				final int contentLength;
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
				} while( bytesRead > 0 );
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
			}

			try
			{
				if( imageStream != null && imageStream.size() > 0 )
				{
					if( findMimeType( GIF_MIME_TYPE, mimeTypes ) )
					{
						byte[] bits = imageStream.toByteArray();
						ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream( bits );
						try
						{
							GifDrawable gifDrawable = new GifDrawable( byteArrayInputStream );
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
						byte[] bits = imageStream.toByteArray();

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

							BitmapDrawable bitmapDrawable = new BitmapDrawable( getResources(), bitmap );
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

		@Override
		protected void onProgressUpdate( final Integer... progress )
		{
			if( progress.length == 2 )
			{
				m_progressBar.setMax( progress[ 1 ] );
				m_progressBar.incrementProgressBy( progress[ 0 ] );
			}
			else
			{
				m_progressBar.setIndeterminate( true );
			}
		}

		@Override
		protected void onPostExecute( final Drawable drawable )
		{
			m_progressBar.setVisibility( View.INVISIBLE );
			m_imageView.setImageDrawable( drawable );
		}

		private boolean isSupportedFormat( final List<String> mimeTypes )
		{
			boolean supported = false;

			for( String supportedMimeType : SUPPORTED_FORMATS )
			{
				if( findMimeType( supportedMimeType, mimeTypes ) )
				{
					supported = true;
					break;
				}
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
}
