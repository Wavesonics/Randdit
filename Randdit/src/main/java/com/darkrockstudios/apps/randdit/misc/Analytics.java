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
				                                "get_next_image",  // Event action (required)
				                                "Get next image",   // Event label
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
				                                "category_selected",  // Event action (required)
				                                "Category changed",   // Event label
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
				                                "download_button",  // Event action (required)
				                                "Image downloaded",   // Event label
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
				                                "wallpaper_button",  // Event action (required)
				                                "Wallpaper set",   // Event label
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
				                                "settings_button",  // Event action (required)
				                                "Settings",   // Event label
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
				                                "share_button",  // Event action (required)
				                                "Post shared",   // Event label
				                                null )            // Event value
				                  .build()
		);
	}

	public static void trackFullscreen( final Context context, final NavDrawerAdapter.NavItem category, final boolean pro )
	{
		EasyTracker easyTracker = EasyTracker.getInstance( context );

		easyTracker.set( Fields.customDimension( 2 ), pro + "" );
		easyTracker.set( Fields.customDimension( 1 ), NavDrawerAdapter.getId( category ) );
		easyTracker.send( MapBuilder
				                  .createEvent( "ui_action",     // Event category (required)
				                                "toggle_fullscreen",  // Event action (required)
				                                "Toggle Fullscreen",   // Event label
				                                null )            // Event value
				                  .build()
		);
	}

	public static void trackProClick( final Context context, final String origin )
	{
		EasyTracker easyTracker = EasyTracker.getInstance( context );

		easyTracker.set( Fields.customDimension( 3 ), origin );
		easyTracker.send( MapBuilder
				                  .createEvent( "ui_action",     // Event category (required)
				                                "purchase_pro",  // Event action (required)
				                                "Pro dialog presented",   // Event label
				                                null )            // Event value
				                  .build()
		);
	}

	public static void trackRateClick( final Context context, final String origin )
	{
		EasyTracker easyTracker = EasyTracker.getInstance( context );

		easyTracker.set( Fields.customDimension( 3 ), origin );
		easyTracker.send( MapBuilder
				                  .createEvent( "ui_action",     // Event category (required)
				                                "rate_app",  // Event action (required)
				                                "Google Play launched to rate the app",   // Event label
				                                null )            // Event value
				                  .build()
		);
	}
}
