package com.darkrockstudios.apps.randdit;

import android.app.Application;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.com.darkrockstudios.views.uriimageview.LruDrawableCache;

/**
 * Created by Adam on 11/11/13.
 */
public class RandditApplication extends Application
{
	private static RequestQueue     s_requestQueue;
	private static LruDrawableCache s_imageCache;

	public void onCreate()
	{
		super.onCreate();

		s_requestQueue = Volley.newRequestQueue( this );
		LruImageCache imageCache = new LruImageCache();
		s_imageCache = new LruDrawableCache();
	}

	public static RequestQueue getRequestQueue()
	{
		return s_requestQueue;
	}

	public static LruDrawableCache getImageCache()
	{
		return s_imageCache;
	}

	private static class LruImageLoader extends ImageLoader
	{
		private final LruImageCache m_imageCache;

		public LruImageLoader( RequestQueue requestQueue, LruImageCache cache )
		{
			super( requestQueue, cache );

			m_imageCache = cache;
		}

		public void clearCache()
		{
			m_imageCache.clear();
		}
	}

	private static class LruImageCache implements ImageLoader.ImageCache
	{
		private final LruCache<String, Bitmap> m_cache;

		public LruImageCache()
		{
			m_cache = new LruCache<>( 10 );
		}

		@Override
		public Bitmap getBitmap( String url )
		{
			return m_cache.get( url );
		}

		@Override
		public void putBitmap( String url, Bitmap bitmap )
		{
			m_cache.put( url, bitmap );
		}

		public void clear()
		{
			m_cache.evictAll();
		}
	}
}
