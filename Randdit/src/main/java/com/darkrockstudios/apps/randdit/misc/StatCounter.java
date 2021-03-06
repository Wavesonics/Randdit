package com.darkrockstudios.apps.randdit.misc;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Adam on 12/8/13.
 */
public class StatCounter
{
	public static void countImageView( final Context context )
	{
		SharedPreferences stats = PreferenceManager.getDefaultSharedPreferences( context );

		long views = Long.parseLong( stats.getString( Preferences.KEY_IMAGE_VIEWS, "0" ) );
		++views;

		SharedPreferences.Editor editor = stats.edit();
		editor.putString( Preferences.KEY_IMAGE_VIEWS, views + "" );
		editor.apply();
	}

	public static long getImageViewCount( final Context context )
	{
		SharedPreferences stats = PreferenceManager.getDefaultSharedPreferences( context );
		return Long.parseLong( stats.getString( Preferences.KEY_IMAGE_VIEWS, "0" ) );
	}
}
