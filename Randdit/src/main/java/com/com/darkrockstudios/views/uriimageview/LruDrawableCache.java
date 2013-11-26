package com.com.darkrockstudios.views.uriimageview;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.LruCache;

/**
 * Created by Adam on 11/22/13.
 */
public class LruDrawableCache
{
	private final LruCache<Uri, Drawable> m_cache;

	public LruDrawableCache()
	{
		m_cache = new LruCache<>( 2 );
	}

	public Drawable getDrawable( Uri uri )
	{
		return m_cache.get( uri );
	}

	public void putDrawable( Uri uri, Drawable bitmap )
	{
		m_cache.put( uri, bitmap );
	}
}
