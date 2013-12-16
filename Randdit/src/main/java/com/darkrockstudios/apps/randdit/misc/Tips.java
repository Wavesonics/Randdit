package com.darkrockstudios.apps.randdit.misc;

import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.preference.PreferenceManager;

import com.darkrockstudios.apps.randdit.fragments.TipFragment;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

/**
 * Created by Adam on 12/15/13.
 */
public class Tips
{
	private static final String TIP_FILE = "tips.json";

	public static class Tip implements Serializable
	{
		public transient static final String TYPE_TIP  = "tip";
		public transient static final String TYPE_PRO  = "pro";
		public transient static final String TYPE_RATE = "rate";

		public String id;
		public String type;
		public long   image_threshold;
		public String title;
		public String body;
	}

	private static List<Tip> s_tips;

	static String convertStreamToString( final java.io.InputStream is )
	{
		final String inputString;
		if( is != null )
		{
			java.util.Scanner s = new java.util.Scanner( is, "utf-8" ).useDelimiter( "\\A" );
			inputString = s.hasNext() ? s.next() : null;
		}
		else
		{
			inputString = null;
		}

		return inputString;
	}

	private static List<Tip> getTips( final Context context )
	{
		if( s_tips == null )
		{
			s_tips = new Vector<>();

			InputStream inputStream = null;
			try
			{
				AssetManager assetManager = context.getAssets();
				inputStream = assetManager.open( TIP_FILE );
				String tipsStr = convertStreamToString( inputStream );

				if( tipsStr != null )
				{
					Gson gson = new Gson();

					JSONObject tipsObj = new JSONObject( tipsStr );
					JSONArray tipsArry = tipsObj.getJSONArray( "tips" );

					for( int ii = 0; ii < tipsArry.length(); ++ii )
					{
						JSONObject tipObj = tipsArry.getJSONObject( ii );
						Tip tip = gson.fromJson( tipObj.toString(), Tip.class );
						s_tips.add( tip );
					}

				}
			}
			catch( IOException | JSONException e )
			{
				e.printStackTrace();
			}
			finally
			{
				if( inputStream != null )
				{
					try
					{
						inputStream.close();
					}
					catch( IOException e )
					{
						e.printStackTrace();
					}
				}
			}
		}

		return s_tips;
	}

	private static List<String> getShownTips( final Context context )
	{
		final List<String> tipIdsShown;

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( context );
		String tipIdsShownStr = settings.getString( Preferences.KEY_TIP_IDS_SHOWN, null );
		if( tipIdsShownStr != null )
		{
			if( tipIdsShownStr.contains( "," ) )
			{
				tipIdsShown = Arrays.asList( tipIdsShownStr.split( "," ) );
			}
			else
			{
				tipIdsShown = new ArrayList<>();
				tipIdsShown.add( tipIdsShownStr );
			}
		}
		else
		{
			tipIdsShown = new ArrayList<>();
		}

		return tipIdsShown;
	}

	public static Tip shouldShowTip( final Context context, boolean isPro )
	{
		Tip tipToShow = null;

		List<Tip> tips = Tips.getTips( context );
		if( tips != null )
		{
			final long imagesViewed = StatCounter.getImageViewCount( context );
			List<String> tipIdsShown = getShownTips( context );

			for( Tip tip : tips )
			{
				if( imagesViewed >= tip.image_threshold && !tipIdsShown.contains( tip.id ) )
				{
					// Don't show pro tips to pro users
					if( tip.type.equals( Tip.TYPE_PRO ) )
					{
						if( !isPro )
						{
							tipToShow = tip;
							break;
						}
					}
					else
					{
						tipToShow = tip;
						break;
					}
				}
			}
		}

		return tipToShow;
	}

	public static void recordTipViewed( final Tip tip, final Context context )
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( context );

		String viewedTips = settings.getString( Preferences.KEY_TIP_IDS_SHOWN, null );

		SharedPreferences.Editor editor = settings.edit();
		if( viewedTips == null )
		{
			viewedTips = tip.id;
		}
		else
		{
			if( !viewedTips.contains( tip.id ) )
			{
				viewedTips += ',' + tip.id;
			}
		}

		editor.putString( Preferences.KEY_TIP_IDS_SHOWN, viewedTips );
		editor.apply();
	}

	public static DialogFragment constructTipDialog( final Tip tip )
	{
		return TipFragment.newInstance( tip );
	}
}
