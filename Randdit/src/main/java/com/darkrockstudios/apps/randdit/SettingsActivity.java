package com.darkrockstudios.apps.randdit;

import android.app.Activity;
import android.os.Bundle;

import com.darkrockstudios.apps.randdit.fragments.SettingsFragment;

/**
 * Created by Adam on 11/23/13.
 */
public class SettingsActivity extends Activity
{
	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		// Display the fragment as the main content.
		getFragmentManager().beginTransaction()
				.replace( android.R.id.content, new SettingsFragment() )
				.commit();
	}
}