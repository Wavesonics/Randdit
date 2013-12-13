package com.darkrockstudios.apps.randdit.misc;

import android.content.Context;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;

/**
 * Created by Adam on 11/24/13.
 */
public final class Analytics
{
	public static void trackScreen( final Context context, final String screenName, final boolean pro )
	{
		EasyTracker easyTracker = EasyTracker.getInstance( context );
		easyTracker.set( Fields.SCREEN_NAME, screenName );
		easyTracker.set( Fields.customDimension( 2 ), pro + "" );
		easyTracker.send( MapBuilder.createAppView().build() );
	}

	public static void trackNextImageClick( final Context context, final NavDrawerAdapter.NavItem category, final boolean pro )
	{
		EasyTracker easyTracker = EasyTracker.getInstance( context );

		easyTracker.set( Fields.customDimension( 1 ), NavDrawerAdapter.getId( category ) );
		easyTracker.set( Fields.customDimension( 2 ), pro + "" );
		easyTracker.send( MapBuilder
				                  .createEvent( "ui_action",     // Event category (required)
				                                "button_press",  // Event action (required)
				                                "get_next_image",   // Event label
				                                null )            // Event value
				                  .build()
		);
	}

	public static void trackCategoryChange( final Context context, final NavDrawerAdapter.NavItem category, final boolean pro )
	{
		EasyTracker easyTracker = EasyTracker.getInstance( context );

		easyTracker.set( Fields.customDimension( 1 ), NavDrawerAdapter.getId( category ) );
		easyTracker.set( Fields.customDimension( 2 ), pro + "" );
		easyTracker.send( MapBuilder
				                  .createEvent( "ui_action",     // Event category (required)
				                                "button_press",  // Event action (required)
				                                "nav_item_button",   // Event label
				                                null )            // Event value
				                  .build()
		);
	}

	public static void trackDownload( final Context context, final NavDrawerAdapter.NavItem category, final boolean pro )
	{
		EasyTracker easyTracker = EasyTracker.getInstance( context );

		easyTracker.set( Fields.customDimension( 1 ), NavDrawerAdapter.getId( category ) );
		easyTracker.set( Fields.customDimension( 2 ), pro + "" );
		easyTracker.send( MapBuilder
				                  .createEvent( "ui_action",     // Event category (required)
				                                "button_press",  // Event action (required)
				                                "download_button",   // Event label
				                                null )            // Event value
				                  .build()
		);
	}

	public static void trackWallpaper( final Context context, final NavDrawerAdapter.NavItem category, final boolean pro )
	{
		EasyTracker easyTracker = EasyTracker.getInstance( context );

		easyTracker.set( Fields.customDimension( 1 ), NavDrawerAdapter.getId( category ) );
		easyTracker.set( Fields.customDimension( 2 ), pro + "" );
		easyTracker.send( MapBuilder
				                  .createEvent( "ui_action",     // Event category (required)
				                                "button_press",  // Event action (required)
				                                "wallpaper_button",   // Event label
				                                null )            // Event value
				                  .build()
		);
	}

	public static void trackSettings( final Context context, final boolean pro )
	{
		EasyTracker easyTracker = EasyTracker.getInstance( context );

		easyTracker.set( Fields.customDimension( 2 ), pro + "" );
		easyTracker.send( MapBuilder
				                  .createEvent( "ui_action",     // Event category (required)
				                                "button_press",  // Event action (required)
				                                "settings_button",   // Event label
				                                null )            // Event value
				                  .build()
		);
	}

	public static void trackShare( final Context context, final NavDrawerAdapter.NavItem category, final boolean pro )
	{
		EasyTracker easyTracker = EasyTracker.getInstance( context );

		easyTracker.set( Fields.customDimension( 2 ), pro + "" );
		easyTracker.set( Fields.customDimension( 1 ), NavDrawerAdapter.getId( category ) );
		easyTracker.send( MapBuilder
				                  .createEvent( "ui_action",     // Event category (required)
				                                "button_press",  // Event action (required)
				                                "settings_button",   // Event label
				                                null )            // Event value
				                  .build()
		);
	}

	public static void trackProClick( final Context context, final NavDrawerAdapter.NavItem category )
	{
		EasyTracker easyTracker = EasyTracker.getInstance( context );

		easyTracker.set( Fields.customDimension( 1 ), NavDrawerAdapter.getId( category ) );
		easyTracker.send( MapBuilder
				                  .createEvent( "ui_action",     // Event category (required)
				                                "button_press",  // Event action (required)
				                                "purchase_pro",   // Event label
				                                null )            // Event value
				                  .build()
		);
	}
}
