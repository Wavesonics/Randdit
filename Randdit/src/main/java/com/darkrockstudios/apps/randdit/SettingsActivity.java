package com.darkrockstudios.apps.randdit;

import android.app.Activity;
import android.app.backup.BackupManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import com.darkrockstudios.apps.randdit.fragments.SettingsFragment;

/**
 * Created by Adam on 11/23/13.
 */
public class SettingsActivity extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener
{
	private static final String TAG = SettingsActivity.class.getSimpleName();

	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		getActionBar().setDisplayHomeAsUpEnabled( true );
		getActionBar().setHomeButtonEnabled( true );

		// Display the fragment as the main content.
		getFragmentManager().beginTransaction()
				.replace( android.R.id.content, new SettingsFragment() )
				.commit();
	}

	@Override
	protected void onStart()
	{
		super.onStart();

		PreferenceManager.getDefaultSharedPreferences( this ).registerOnSharedPreferenceChangeListener( this );
	}

	@Override
	protected void onStop()
	{
		super.onStop();

		PreferenceManager.getDefaultSharedPreferences( this ).unregisterOnSharedPreferenceChangeListener( this );
	}

	@Override
	public boolean onOptionsItemSelected( final MenuItem item )
	{
		final boolean handled;
		if( item.getItemId() == android.R.id.home )
		{
			finish();
			handled = true;
		}
		else
		{
			handled = false;
		}

		return handled;
	}

	@Override
	public void onSharedPreferenceChanged( SharedPreferences sharedPreferences, String key )
	{
		BackupManager backupManager = new BackupManager( this );
		backupManager.dataChanged();
	}
}