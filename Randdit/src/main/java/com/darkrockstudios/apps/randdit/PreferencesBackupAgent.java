package com.darkrockstudios.apps.randdit;

import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;

/**
 * Created by adam on 12/7/13.
 */
public class PreferencesBackupAgent extends BackupAgentHelper {

    static final String PREFERENCES_BACKUP_KEY = "preferences";

    private String getDefaultSharedPreferenceKey() {
        return "com.darkrockstudios.apps.randdit_preferences";
    }

    // Simply allocate a helper and install it
    @Override
    public void onCreate() {
        SharedPreferencesBackupHelper helper =
                new SharedPreferencesBackupHelper(this, getDefaultSharedPreferenceKey());
        addHelper(PREFERENCES_BACKUP_KEY, helper);
    }
}