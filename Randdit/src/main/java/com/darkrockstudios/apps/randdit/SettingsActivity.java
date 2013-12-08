package com.darkrockstudios.apps.randdit;

import android.app.Activity;
import android.app.backup.BackupManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.darkrockstudios.apps.randdit.fragments.SettingsFragment;

/**
 * Created by Adam on 11/23/13.
 */
public class SettingsActivity extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener {
	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		// Display the fragment as the main content.
		getFragmentManager().beginTransaction()
				.replace( android.R.id.content, new SettingsFragment() )
				.commit();
	}

    @Override
    protected void onStart() {
        super.onStart();

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        BackupManager.dataChanged(RandditApplication.class.getPackage().getName());
    }
}