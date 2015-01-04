package com.darkrockstudios.apps.randdit.misc;

import android.os.Build;

/**
 * Created by Adam on 11/9/2014.
 */
public class OsUtils
{
	public static boolean atLeastLollipop()
	{
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
	}
}