package com.darkrockstudios.apps.randdit.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.darkrockstudios.apps.randdit.R;

/**
 * Created by Adam on 11/23/13.
 */
public class IntroFragment extends Fragment
{
	public static IntroFragment newInstance()
	{
		return new IntroFragment();
	}

	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
	{
		View view = inflater.inflate( R.layout.intro_fragment, container, false );

		return view;
	}
}
