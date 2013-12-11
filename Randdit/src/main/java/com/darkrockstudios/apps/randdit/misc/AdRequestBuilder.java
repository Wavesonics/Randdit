package com.darkrockstudios.apps.randdit.misc;

import com.google.android.gms.ads.AdRequest;

/**
 * Created by Adam on 12/10/13.
 */
public class AdRequestBuilder
{
	public static AdRequest buildRequest()
	{
		AdRequest.Builder adBuilder = new AdRequest.Builder();
		adBuilder.addTestDevice( AdRequest.DEVICE_ID_EMULATOR );
		adBuilder.addTestDevice( "CAA9C926FB13FA7FEE6E98B745B46A39" ); // My GN
		adBuilder.addTestDevice( "ADD12FD43C2481D9F9BACE38F280D31B" ); // MY TF201, Me
		adBuilder.addTestDevice( "36CDCE2BA7F710EB8E8F68E3A46F780C" ); // My TF201, DRS
		adBuilder.tagForChildDirectedTreatment( false );

		return adBuilder.build();
	}
}
