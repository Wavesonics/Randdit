package com.darkrockstudios.apps.randdit;

import android.app.backup.BackupManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import com.darkrockstudios.apps.randdit.fragments.SettingsFragment;
import com.darkrockstudios.apps.randdit.googleplaygames.BaseGameActivity;
import com.darkrockstudios.apps.randdit.misc.Preferences;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

/**
 * Created by Adam on 11/23/13.
 */
public class SettingsActivity extends BaseGameActivity implements SharedPreferences.OnSharedPreferenceChangeListener
{
	private static final String TAG             = SettingsActivity.class.getSimpleName();
	private static final String ACTION_SIGN_OUT = "com.darkrockstudios.apps.randdit.SIGN_OUT";

	private SettingReceiver m_receiver;
	private boolean         m_isPro;

	@Override
	protected void onCreate( final Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		getSupportActionBar().setDisplayHomeAsUpEnabled( true );
		getSupportActionBar().setHomeButtonEnabled( true );

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( this );
		m_isPro = settings.getBoolean( Preferences.KEY_IS_PRO, false );

		// Display the fragment as the main content.
		getFragmentManager().beginTransaction()
		                    .replace( android.R.id.content, SettingsFragment.newInstance( m_isPro ) )
		                    .commit();
	}

	@Override
	protected void onStart()
	{
		super.onStart();

		PreferenceManager.getDefaultSharedPreferences( this ).registerOnSharedPreferenceChangeListener( this );

		IntentFilter filter = new IntentFilter();
		filter.addAction( ACTION_SIGN_OUT );

		m_receiver = new SettingReceiver();
		registerReceiver( m_receiver, filter );
	}

	@Override
	protected void onStop()
	{
		super.onStop();

		PreferenceManager.getDefaultSharedPreferences( this ).unregisterOnSharedPreferenceChangeListener( this );

		if( m_receiver != null )
		{
			unregisterReceiver( m_receiver );
			m_receiver = null;
		}
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
	public void onSharedPreferenceChanged( final SharedPreferences sharedPreferences, final String key )
	{
		BackupManager backupManager = new BackupManager( this );
		backupManager.dataChanged();
	}

	@Override
	public void onSignInFailed()
	{

	}

	@Override
	public void onSignInSucceeded()
	{

	}

	private class SettingReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive( final Context context, final Intent intent )
		{
			if( ACTION_SIGN_OUT.equals( intent.getAction() ) )
			{
				if( isSignedIn() )
				{
					Crouton.makeText( SettingsActivity.this, R.string.toast_signout, Style.CONFIRM ).show();
					signOut();
				}
				else
				{
					Crouton.makeText( SettingsActivity.this, R.string.toast_not_signed_in, Style.ALERT ).show();
				}
			}
		}
	}
}