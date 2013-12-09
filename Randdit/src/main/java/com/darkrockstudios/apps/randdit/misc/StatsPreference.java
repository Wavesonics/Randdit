package com.darkrockstudios.apps.randdit.misc;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;

/**
 * Created by Adam on 12/8/13.
 */
public class StatsPreference extends ListPreference
{
	public StatsPreference( final Context context, final AttributeSet attrs )
	{
		super( context, attrs );
	}

	public StatsPreference( final Context context )
	{
		super( context );
	}

	@Override
	public void setValue( final String value )
	{
		super.setValue( value );
		setSummary( value );
	}
}
