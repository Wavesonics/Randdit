package com.darkrockstudios.apps.randdit.misc;

import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Adam on 1/28/2015.
 */
public class HackyDrawerLayout extends DrawerLayout
{
	public HackyDrawerLayout( Context context )
	{
		super( context );
	}

	public HackyDrawerLayout( Context context, AttributeSet attrs )
	{
		super( context, attrs );
	}

	public HackyDrawerLayout( Context context, AttributeSet attrs, int defStyle )
	{
		super( context, attrs, defStyle );
	}

	@Override
	public boolean onTouchEvent( final MotionEvent ev )
	{
		try
		{
			return super.onTouchEvent( ev );
		}
		catch( ArrayIndexOutOfBoundsException | IllegalArgumentException ex )
		{
			ex.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean onInterceptTouchEvent( final MotionEvent ev )
	{
		try
		{
			return super.onInterceptTouchEvent( ev );
		}
		catch( ArrayIndexOutOfBoundsException | IllegalArgumentException ex )
		{
			ex.printStackTrace();
		}
		return false;
	}
}
