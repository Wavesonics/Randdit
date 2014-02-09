package com.darkrockstudios.apps.randdit.misc;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;

/**
 * Created by Adam on 2/8/14.
 */
public class BroadcastPreference extends Preference implements Preference.OnPreferenceClickListener
{
	public BroadcastPreference( Context context, AttributeSet attrs )
	{
		super( context, attrs );

		this.setOnPreferenceClickListener( this );
	}

	@Override
	public boolean onPreferenceClick( Preference preference )
	{
		getContext().sendBroadcast( getIntent() );
		return true;
	}
}