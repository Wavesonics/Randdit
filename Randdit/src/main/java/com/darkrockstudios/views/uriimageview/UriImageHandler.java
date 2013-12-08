package com.darkrockstudios.views.uriimageview;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import android.widget.ProgressBar;

/**
 * Created by Adam on 11/25/13.
 */
public class UriImageHandler
{
	private UriImageView         m_imageView;
	private UriImageDownloadTask m_downloadTask;
	private Drawable             m_pendingDrawable;
	private int                  m_maxProgress;
	private int                  m_currentProgress;

	public UriImageHandler()
	{

	}

	private synchronized void update( final UriImageView imageView )
	{
		m_imageView = imageView;
		if( m_pendingDrawable != null )
		{
			m_imageView.setDrawable( m_pendingDrawable );
			m_imageView.setDisplaying();
			m_pendingDrawable = null;
		}
		else if( m_downloadTask != null )
		{
			ProgressBar progressBar = m_imageView.getProgressBar();
			progressBar.setMax( m_maxProgress );
			progressBar.setProgress( m_currentProgress );
			m_imageView.setLoading();
		}
	}

	public synchronized void onResume( final UriImageView imageView )
	{
		update( imageView );
	}

	public synchronized void onPause()
	{
		m_imageView = null;
	}

	public UriImageDownloadTask getDownloadTask()
	{
		return m_downloadTask;
	}

	public void cancelDownload()
	{
		if( m_downloadTask != null )
		{
			m_downloadTask.cancel();
			m_downloadTask = null;
		}
	}

	public synchronized void loadImage( final Uri uri, final UriImageView imageView, final LruDrawableCache cache )
	{
		update( imageView );

		if( m_downloadTask != null )
		{
			Uri downloadUri = m_downloadTask.getUri();
			if( downloadUri != null && !uri.equals( downloadUri ) )
			{
				m_downloadTask.cancel();
			}
		}
		m_downloadTask = m_imageView.loadImage( uri, cache, this );
	}

	public synchronized UriImageView getImageView()
	{
		return m_imageView;
	}

	public synchronized void onProgressUpdate( final Integer... progress )
	{
		if( m_imageView != null )
		{
			ProgressBar progressBar = m_imageView.getProgressBar();
			if( progress.length == 2 )
			{
				progressBar.setMax( progress[ 1 ] );
				progressBar.incrementProgressBy( progress[ 0 ] );

				m_maxProgress = progress[ 1 ];
				m_currentProgress += progress[ 0 ];
			}
			else
			{
				progressBar.setIndeterminate( true );
			}
		}
		else if( progress.length > 0 )
		{
			m_currentProgress += progress[ 0 ];
		}
	}

	protected synchronized void onPostExecute( final Drawable drawable )
	{
		m_downloadTask = null;
		m_currentProgress = 0;

		if( m_imageView != null )
		{
			ProgressBar progressBar = m_imageView.getProgressBar();
			progressBar.setVisibility( View.INVISIBLE );
			m_imageView.setDrawable( drawable );
			m_imageView.setDisplaying();
		}
		else
		{
			m_pendingDrawable = drawable;
		}
	}
}
