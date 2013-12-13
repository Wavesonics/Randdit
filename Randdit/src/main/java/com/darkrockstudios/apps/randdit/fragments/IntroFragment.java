package com.darkrockstudios.apps.randdit.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.darkrockstudios.apps.randdit.R;
import com.darkrockstudios.apps.randdit.misc.Analytics;
import com.darkrockstudios.apps.randdit.misc.NextButtonEnabler;

/**
 * Created by Adam on 11/23/13.
 */
public class IntroFragment extends Fragment implements NextButtonEnabler
{
	private static final String ARG_PRO = PostFragment.class.getName() + ".PRO";

	private boolean m_isPro;
	private Button  m_getStartedButton;

	public static IntroFragment newInstance( final boolean isPro )
	{
		IntroFragment fragment = new IntroFragment();

		Bundle args = new Bundle();
		args.putBoolean( ARG_PRO, isPro );
		fragment.setArguments( args );

		return fragment;
	}

	@Override
	public void onCreate( final Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		Bundle args = getArguments();
		if( args != null )
		{
			m_isPro = args.getBoolean( ARG_PRO );
		}

		Analytics.trackScreen( getActivity(), IntroFragment.class.getSimpleName(), m_isPro );
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
