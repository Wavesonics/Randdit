package com.darkrockstudios.apps.randdit.misc;

import com.darkrockstudios.apps.randdit.RandditApplication;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;


/**
 * Created by Adam on 11/24/13.
 */
public final class Analytics
{
	public static final int DIMENSION_CATEGORY_NAME = 1;
	public static final int DIMENSION_PRO           = 2;
	public static final int DIMENSION_ORIGIN        = 3;

	public static void trackNextImageClick( final NavDrawerAdapter.NavItem category, final boolean pro )
	{
		Tracker tracker = RandditApplication.getAnalyticsTracker();
		tracker.send( new HitBuilders.EventBuilder()
				              .setCustomDimension( DIMENSION_CATEGORY_NAME, NavDrawerAdapter.getId( category ) )
				              .setCustomDimension( DIMENSION_PRO, pro + "" )
				              .setCategory( "ui_action" )
				              .setLabel( "Get next image" )
				              .setAction( "get_next_image" )
				              .build() );
	}

	public static void trackCategoryChange( final NavDrawerAdapter.NavItem category, final boolean pro )
	{
		Tracker tracker = RandditApplication.getAnalyticsTracker();
		tracker.send( new HitBuilders.EventBuilder()
				              .setCustomDimension( DIMENSION_CATEGORY_NAME, NavDrawerAdapter.getId( category ) )
				              .setCustomDimension( DIMENSION_PRO, pro + "" )
				              .setCategory( "ui_action" )
				              .setAction( "category_selected" )
				              .setLabel( "Category changed" )
				              .build() );
	}

	public static void trackDownload( final NavDrawerAdapter.NavItem category, final boolean pro )
	{
		Tracker tracker = RandditApplication.getAnalyticsTracker();
		tracker.send( new HitBuilders.EventBuilder()
				              .setCustomDimension( DIMENSION_CATEGORY_NAME, NavDrawerAdapter.getId( category ) )
				              .setCustomDimension( DIMENSION_PRO, pro + "" )
				              .setCategory( "ui_action" )
				              .setAction( "download_button" )
				              .setLabel( "Image downloaded" )
				              .build() );
	}

	public static void trackWallpaper( final NavDrawerAdapter.NavItem category, final boolean pro )
	{
		Tracker tracker = RandditApplication.getAnalyticsTracker();
		tracker.send( new HitBuilders.EventBuilder()
				              .setCustomDimension( DIMENSION_CATEGORY_NAME, NavDrawerAdapter.getId( category ) )
				              .setCustomDimension( DIMENSION_PRO, pro + "" )
				              .setCategory( "ui_action" )
				              .setAction( "wallpaper_button" )
				              .setLabel( "Wallpaper set" )
				              .build() );
	}

	public static void trackSettings( final boolean pro )
	{
		Tracker tracker = RandditApplication.getAnalyticsTracker();
		tracker.send( new HitBuilders.EventBuilder()
				              .setCustomDimension( DIMENSION_PRO, pro + "" )
				              .setCategory( "ui_action" )
				              .setAction( "settings_button" )
				              .setLabel( "Settings" )
				              .build() );
	}

	public static void trackShare( final NavDrawerAdapter.NavItem category, final boolean pro )
	{
		Tracker tracker = RandditApplication.getAnalyticsTracker();
		tracker.send( new HitBuilders.EventBuilder()
				              .setCustomDimension( DIMENSION_CATEGORY_NAME, NavDrawerAdapter.getId( category ) )
				              .setCustomDimension( DIMENSION_PRO, pro + "" )
				              .setCategory( "ui_action" )
				              .setAction( "share_button" )
				              .setLabel( "Post shared" )
				              .build() );
	}

	public static void trackFullscreen( final NavDrawerAdapter.NavItem category, final boolean pro )
	{
		Tracker tracker = RandditApplication.getAnalyticsTracker();
		tracker.send( new HitBuilders.EventBuilder()
				              .setCustomDimension( DIMENSION_CATEGORY_NAME, NavDrawerAdapter.getId( category ) )
				              .setCustomDimension( DIMENSION_PRO, pro + "" )
				              .setCategory( "ui_action" )
				              .setAction( "toggle_fullscreen" )
				              .setLabel( "Toggle Fullscreen" )
				              .build() );
	}

	public static void trackProClick( final String origin )
	{
		Tracker tracker = RandditApplication.getAnalyticsTracker();
		tracker.send( new HitBuilders.EventBuilder()
				              .setCustomDimension( DIMENSION_ORIGIN, origin )
				              .setCategory( "ui_action" )
				              .setAction( "purchase_pro" )
				              .setLabel( "Pro dialog presented" )
				              .build() );
	}

	public static void trackRateClick( final String origin )
	{
		Tracker tracker = RandditApplication.getAnalyticsTracker();
		tracker.send( new HitBuilders.EventBuilder()
				              .setCustomDimension( DIMENSION_ORIGIN, origin )
				              .setCategory( "ui_action" )
				              .setAction( "rate_app" )
				              .setLabel( "Google Play launched to rate the app" )
				              .build() );
	}

	public static void trackAboutClick( final NavDrawerAdapter.NavItem category )
	{
		Tracker tracker = RandditApplication.getAnalyticsTracker();
		tracker.send( new HitBuilders.EventBuilder()
				              .setCustomDimension( DIMENSION_CATEGORY_NAME, NavDrawerAdapter.getId( category ) )
				              .setCategory( "ui_action" )
				              .setAction( "about" )
				              .setLabel( "About displayed" )
				              .build() );
	}

	public static void trackViewOtherAppsClick()
	{
		Tracker tracker = RandditApplication.getAnalyticsTracker();
		tracker.send( new HitBuilders.EventBuilder()
				              .setCategory( "ui_action" )
				              .setAction( "view_other_apps" )
				              .setLabel( "View other Dark Rock Studios apps in Play Store" )
				              .build() );
	}
}
