package com.darkrockstudios.apps.randdit.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.darkrockstudios.apps.randdit.R;
import com.darkrockstudios.apps.randdit.misc.NextButtonEnabler;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;

/**
 * Created by Adam on 11/23/13.
 */
public class IntroFragment extends Fragment implements NextButtonEnabler
{
	private Button m_getStartedButton;

	public static IntroFragment newInstance()
	{
		return new IntroFragment();
	}

	@Override
	public void onCreate( final Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		EasyTracker tracker = EasyTracker.getInstance( getActivity() );
		tracker.set( Fields.SCREEN_NAME, IntroFragment.class.getSimpleName() );
		tracker.send( MapBuilder.createAppView().build() );
	}

	@Override
	public View onCreateView( final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState )
	{
		View view = inflater.inflate( R.layout.intro_fragment, container, false );

		m_getStartedButton = (Button) view.findViewById( R.id.INTRO_load_image_button );

		return view;
	}

	@Override
	public void setNextButtonEnabled( final boolean enabled )
	{
		if( isAdded() && m_getStartedButton != null )
		{
			m_getStartedButton.setEnabled( enabled );
		}
	}
}
