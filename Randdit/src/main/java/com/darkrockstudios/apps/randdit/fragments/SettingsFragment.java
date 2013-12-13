package com.darkrockstudios.apps.randdit.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.darkrockstudios.apps.randdit.BuildConfig;
import com.darkrockstudios.apps.randdit.R;

/**
 * Created by Adam on 11/23/13.
 */
public class SettingsFragment extends PreferenceFragment
{
	private static final String ARG_PRO = PostFragment.class.getName() + ".PRO";

	private boolean m_isPro;

	public static SettingsFragment newInstance( final boolean isPro )
	{
		SettingsFragment fragment = new SettingsFragment();

		Bundle args = new Bundle();
		args.putBoolean( ARG_PRO, isPro );
		fragment.setArguments( args );

		return fragment;
	}

	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		Bundle args = getArguments();
		if( args != null )
		{
			m_isPro = args.getBoolean( ARG_PRO );
		}

		if( BuildConfig.DEBUG )
		{
			addPreferencesFromResource( R.xml.settings_debug );
		}
		else if( m_isPro )
		{
			addPreferencesFromResource( R.xml.settings_pro );
		}
		else
		{
			addPreferencesFromResource( R.xml.settings );
		}
	}
}