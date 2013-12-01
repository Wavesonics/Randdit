package com.com.darkrockstudios.views.uriimageview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by Adam on 11/22/13.
 */
public class UriImageView extends FrameLayout implements PhotoViewAttacher.OnMatrixChangedListener
{
	private static final String TAG = UriImageView.class.getSimpleName();

	private LruDrawableCache m_cache;

	private Uri m_uri;

	private PhotoView   m_imageView;
	private ProgressBar m_progressBar;

	private int m_errorImageId;

	private static final int DEFAULT_MAX_SIZE = 2048;
	private static boolean m_maxDimensionsSet;
	private static int m_maxBitmapWidth  = DEFAULT_MAX_SIZE;
	private static int m_maxBitmapHeight = DEFAULT_MAX_SIZE;

	private ImageZoomListener m_zoomListener;
	private boolean           m_zooming;

	public static interface ImageZoomListener
	{
		public void beganZooming( UriImageView uriImageView );

		public void endedZooming( UriImageView uriImageView );
	}

	private void setupViews( final Context context, final AttributeSet attrs, final int defStyle )
	{
		m_maxDimensionsSet = false;

		setClipToPadding( false );
		setClipChildren( false );

		m_imageView = new PhotoView( context );
		m_imageView.setAdjustViewBounds( true );
		addView( m_imageView );

		m_imageView.setOnMatrixChangeListener( this );

		m_progressBar = new ProgressBar( context, null, android.R.attr.progressBarStyleHorizontal );
		m_progressBar.setIndeterminate( false );
		m_progressBar.setVisibility( View.INVISIBLE );
		addView( m_progressBar );
	}

	public UriImageView( final Context context, final AttributeSet attrs, final int defStyle )
	{
		super( context, attrs, defStyle );
		setupViews( context, attrs, defStyle );
	}

	public UriImageView( final Context context, final AttributeSet attrs )
	{
		super( context, attrs );
		setupViews( context, attrs, 0 );
	}

	public UriImageView( final Context context )
	{
		super( context );
		setupViews( context, null, 0 );
	}

	public void setZoomListener( ImageZoomListener zoomListener )
	{
		m_zoomListener = zoomListener;
	}

	public void setErrorImage( final int errorImageId )
	{
		m_errorImageId = errorImageId;
	}

	public int getErrorImageId()
	{
		return m_errorImageId;
	}

	public LruDrawableCache getCache()
	{
		return m_cache;
	}

	public int getMaxBitmapWidth()
	{
		return m_maxBitmapWidth;
	}

	public int getMaxBitmapHeight()
	{
		return m_maxBitmapHeight;
	}

	public void setDrawable( final Drawable drawable )
	{
		m_imageView.setImageDrawable( drawable );
	}

	public ProgressBar getProgressBar()
	{
		return m_progressBar;
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

	public void setLoading()
	{
		m_progressBar.setVisibility( View.VISIBLE );
		m_imageView.setVisibility( View.GONE );
	}

	public void setDisplaying()
	{
		m_progressBar.setVisibility( View.GONE );
		m_imageView.setVisibility( View.VISIBLE );
	}

	public UriImageDownloadTask loadImage( final Uri uri, final LruDrawableCache cache, final UriImageHandler uriImageHandler )
	{
		final UriImageDownloadTask downloadTask;

		m_cache = cache;

		if( uri != null && !uri.equals( m_uri ) && uriImageHandler.getDownloadTask() == null )
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
				downloadTask = new UriImageDownloadTask( uriImageHandler, m_uri, getContext() );
				downloadTask.execute( m_uri );
			}
			else
			{
				m_imageView.setImageDrawable( drawable );
				downloadTask = null;
			}
		}
		else
		{
			m_imageView.setImageDrawable( null );
			downloadTask = null;
		}

		return downloadTask;
	}

	@Override
	public void onMatrixChanged( RectF rectF )
	{
		if( m_zoomListener != null )
		{
			final float scale = m_imageView.getScale();
			if( !m_zooming && scale > 1.0f )
			{
				m_zooming = true;
				m_zoomListener.beganZooming( this );
			}
			else if( m_zooming && scale <= 1.0f )
			{
				m_zooming = false;
				m_zoomListener.endedZooming( this );
			}
		}
	}
}
