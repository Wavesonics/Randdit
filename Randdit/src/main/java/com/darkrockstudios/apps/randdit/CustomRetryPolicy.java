package com.darkrockstudios.apps.randdit;

import com.android.volley.DefaultRetryPolicy;

/**
 * Created by Adam on 4/5/2015.
 */
public class CustomRetryPolicy extends DefaultRetryPolicy
{
	public CustomRetryPolicy()
	{
		super( 6000,
		       DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
		       DefaultRetryPolicy.DEFAULT_BACKOFF_MULT );
	}
}
