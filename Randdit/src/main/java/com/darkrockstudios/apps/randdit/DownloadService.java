package com.darkrockstudios.apps.randdit;

import android.app.DownloadManager;
import android.app.Service;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.io.FileInputStream;
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
		final long    m_downloadId;
		final boolean m_setWallpaper;

		public Download( final long downloadId, final boolean setWallpaper )
		{
			m_downloadId = downloadId;
			m_setWallpaper = setWallpaper;
		}
	}

	private Map<Long, Download> m_downloads;

	private DownloadReceiver m_downloadReceiver;

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

			Download download = new Download( downloadId, setWallpaper );
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
		@Override
		public void onReceive( final Context context, final Intent intent )
		{
			String action = intent.getAction();
			if( DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals( action ) )
			{
				long downloadId = intent.getLongExtra( DownloadManager.EXTRA_DOWNLOAD_ID, 0 );

				final Download download = m_downloads.get( downloadId );
				if( download != null )
				{
					m_downloads.remove( download.m_downloadId );

					if( !download.m_setWallpaper )
					{
						Toast.makeText( DownloadService.this, getString( R.string.toast_download_finished ),
						                Toast.LENGTH_SHORT ).show();
					}
					else
					{
						WallpaperManager wallpaperManager = WallpaperManager.getInstance( context );
						DownloadManager downloadManager = (DownloadManager) getSystemService( DOWNLOAD_SERVICE );

						try
						{
							ParcelFileDescriptor pfd = downloadManager.openDownloadedFile( download.m_downloadId );
							FileDescriptor fd = pfd.getFileDescriptor();
							InputStream fileStream = new FileInputStream( fd );
							wallpaperManager.setStream( fileStream );

							Toast.makeText( DownloadService.this, getString( R.string.toast_wallpaper_success ),
							                Toast.LENGTH_SHORT ).show();
						}
						catch( IOException e )
						{
							e.printStackTrace();

							Toast.makeText( DownloadService.this, getString( R.string.toast_wallpaper_failed ),
							                Toast.LENGTH_SHORT ).show();
						}
					}
				}
			}
		}
	}
}
