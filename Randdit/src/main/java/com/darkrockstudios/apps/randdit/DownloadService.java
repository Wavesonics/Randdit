package com.darkrockstudios.apps.randdit;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Adam on 11/23/13.
 */
public class DownloadService extends Service
{
	public static final String EXTRA_SET_WALLPAPER = DownloadService.class.getPackage() + ".SET_WALLPAPER";

	public IBinder onBind( final Intent intent )
	{
		return null;
	}

	private static class Download
	{
		final Uri     m_uri;
		final long    m_downloadId;
		final boolean m_setWallpaper;

		public Download( final Uri uri, final long downloadId, final boolean setWallpaper )
		{
			m_uri = uri;
			m_downloadId = downloadId;
			m_setWallpaper = setWallpaper;
		}
	}

	private Map<Long, Download> m_downloads;

	private DownloadReceiver    m_downloadReceiver;
	private NotificationManager m_notificationManager;

	private void registerReceiver()
	{
		m_downloadReceiver = new DownloadReceiver();
		IntentFilter intentFilter = new IntentFilter( DownloadManager.ACTION_DOWNLOAD_COMPLETE );
		registerReceiver( m_downloadReceiver, intentFilter );
	}

	private void unregisterReceiver()
	{
		if( m_downloadReceiver != null )
		{
			unregisterReceiver( m_downloadReceiver );
			m_downloadReceiver = null;
		}
	}

	@Override
	public void onCreate()
	{
		super.onCreate();

		m_notificationManager = (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );

		m_downloads = new HashMap<>();

		registerReceiver();
	}

	@Override
	public int onStartCommand( final Intent intent, final int flags, final int startId )
	{
		if( intent != null && intent.getData() != null )
		{
			Toast.makeText( DownloadService.this, getString( R.string.toast_download_started ), Toast.LENGTH_SHORT ).show();

			Uri uri = intent.getData();

			boolean setWallpaper = intent.getBooleanExtra( EXTRA_SET_WALLPAPER, false );

			DownloadManager downloadManager = (DownloadManager) getSystemService( DOWNLOAD_SERVICE );
			DownloadManager.Request request = new DownloadManager.Request( uri );
			long downloadId = downloadManager.enqueue( request );

			Download download = new Download( uri, downloadId, setWallpaper );
			m_downloads.put( downloadId, download );
		}

		return START_STICKY;
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();

		unregisterReceiver();
	}

	private class DownloadReceiver extends BroadcastReceiver
	{
		private final boolean IS_JB_OR_LATER = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;

		private static final int ICON_SIZE    = 128;
		private static final int PICTURE_SIZE = 256;

		@Override
		public void onReceive( final Context context, final Intent intent )
		{
			String action = intent.getAction();
			if( DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals( action ) )
			{
				long downloadId = intent.getLongExtra( DownloadManager.EXTRA_DOWNLOAD_ID, 0 );

				DownloadManager downloadManager = (DownloadManager) getSystemService( DOWNLOAD_SERVICE );

				final Download download = m_downloads.get( downloadId );
				if( download != null )
				{
					m_downloads.remove( download.m_downloadId );

					if( !download.m_setWallpaper )
					{
						String successText = getString( R.string.toast_download_finished );
						Toast.makeText( DownloadService.this, successText,
						                Toast.LENGTH_SHORT ).show();

						displayNotification( download, downloadManager, successText );
					}
					else
					{
						WallpaperManager wallpaperManager = WallpaperManager.getInstance( context );

						InputStream inputStream = null;
						try
						{
							inputStream = getDownloadStream( download, downloadManager );
							wallpaperManager.setStream( inputStream );

							String successText = getString( R.string.toast_wallpaper_success );
							Toast.makeText( DownloadService.this, successText,
							                Toast.LENGTH_SHORT ).show();

							displayNotification( download, downloadManager, successText );
						}
						catch( IOException e )
						{
							e.printStackTrace();

							if( inputStream != null )
							{
								try
								{
									inputStream.close();
								}
								catch( IOException e1 )
								{
									e1.printStackTrace();
								}
							}

							Toast.makeText( DownloadService.this, getString( R.string.toast_wallpaper_failed ),
							                Toast.LENGTH_SHORT ).show();
						}
					}
				}
			}
		}

		private void displayNotification( final Download download, final DownloadManager downloadManager, final String title )
		{
			NotificationCompat.Builder builder = new NotificationCompat.Builder( DownloadService.this );
			builder.setSmallIcon( R.drawable.stat_downloaded );
			builder.setContentTitle( title );
			builder.setAutoCancel( true );

			NotificationCompat.BigPictureStyle style = new NotificationCompat.BigPictureStyle();
			InputStream inputStream = null;
			try
			{
				final Bitmap icon;
				if( IS_JB_OR_LATER )
				{
					icon = BitmapFactory.decodeResource( DownloadService.this.getResources(), R.drawable.ic_launcher );
				}
				else
				{
					inputStream = getDownloadStream( download, downloadManager );
					icon = getScaledBitmap( inputStream, ICON_SIZE );
					inputStream.close();
				}

				if( icon != null )
				{
					builder.setLargeIcon( icon );
				}

				if( IS_JB_OR_LATER )
				{
					inputStream = getDownloadStream( download, downloadManager );
					Bitmap bigPicture = getScaledBitmap( inputStream, PICTURE_SIZE );
					if( bigPicture != null )
					{
						style.bigPicture( bigPicture );
					}
				}
			}
			catch( IOException e )
			{
				e.printStackTrace();
			}
			finally
			{
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

			builder.setStyle( style );

			// This is some crappy transformation required for other apps who receive the intent
			// to be able to open the URI
			Uri downloadUri = downloadManager.getUriForDownloadedFile( download.m_downloadId );
			String path = getRealPathFromURI( downloadUri );
			Uri pathUri = Uri.parse( path );
			Uri contentUri = Uri.parse("file://" + pathUri.getPath());

			String mimeType = downloadManager.getMimeTypeForDownloadedFile( download.m_downloadId );

			Intent contentIntent = new Intent( android.content.Intent.ACTION_VIEW );
			contentIntent.setDataAndType( contentUri, mimeType );
			PendingIntent pendingIntent = PendingIntent.getActivity( DownloadService.this, 0, contentIntent, 0 );
			builder.setContentIntent( pendingIntent );

			Notification notification = builder.build();
			m_notificationManager.notify( (int) download.m_downloadId, notification );
		}

		public String getRealPathFromURI(Uri contentUri)
		{
			// can post image
			String [] proj={ MediaStore.Images.Media.DATA};
			Cursor cursor = getContentResolver().query(
					                      contentUri,
			                              proj, // Which columns to return
			                              null,       // WHERE clause; which rows to return (all rows)
			                              null,       // WHERE clause selection arguments (none)
			                              null); // Order-by clause (ascending by name)
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();

			return cursor.getString(column_index);
		}

		private Bitmap getScaledBitmap( final InputStream inputStream, final int maxSize ) throws IOException
		{
			Bitmap bitmap = BitmapFactory.decodeStream( inputStream );
			if( bitmap != null )
			{
				int width = bitmap.getWidth();
				int height = bitmap.getHeight();

				final double scale;
				if( width > height )
				{
					scale = (double) maxSize / (double) width;
				}
				else
				{
					scale = (double) maxSize / (double) height;
				}

				width = (int) ((double) width * scale);
				height = (int) ((double) height * scale);

				bitmap = Bitmap.createScaledBitmap( bitmap, width, height, false );
			}

			return bitmap;
		}

		private InputStream getDownloadStream( final Download download, final DownloadManager downloadManager ) throws FileNotFoundException
		{
			InputStream inputStream = null;

			Uri uri = downloadManager.getUriForDownloadedFile( download.m_downloadId );
			if( uri != null )
			{
				ContentResolver cr = getContentResolver();
				inputStream = cr.openInputStream( uri );
			}

			return inputStream;
		}
	}
}
