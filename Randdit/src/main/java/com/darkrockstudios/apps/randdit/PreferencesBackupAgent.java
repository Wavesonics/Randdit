package com.darkrockstudios.apps.randdit;

import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;

/**
 * Created by adam on 12/7/13.
 */
public class PreferencesBackupAgent extends BackupAgentHelper
{
	private static final String TAG = PreferencesBackupAgent.class.getSimpleName();

	private static final String PREFERENCES_BACKUP_KEY = PreferencesBackupAgent.class.getName() + ".PREFERENCES";

	private String getDefaultSharedPreferenceKey()
	{
		return "com.darkrockstudios.apps.randdit_preferences";
	}

	@Override
	public void onCreate()
	{
		SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper( this, getDefaultSharedPreferenceKey() );
		addHelper( PREFERENCES_BACKUP_KEY, helper );
	}
}