package com.darkrockstudios.apps.randdit.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.darkrockstudios.apps.randdit.R;
import com.darkrockstudios.apps.randdit.misc.PurchaseScreenProvider;
import com.darkrockstudios.apps.randdit.misc.Tips;

/**
 * Created by Adam on 12/15/13.
 */
public class TipFragment extends DialogFragment implements View.OnClickListener
{
	public static final String ARG_TIP = TipFragment.class.getPackage() + ".TIP";

	private Tips.Tip m_tip;

	private PurchaseScreenProvider m_purchaseScreenProvider;

	public static TipFragment newInstance( final Tips.Tip tip )
	{
		TipFragment fragment = new TipFragment();

		Bundle args = new Bundle();
		args.putSerializable( ARG_TIP, tip );
		fragment.setArguments( args );

		return fragment;
	}

	@Override
	public void onAttach( final Activity activity )
	{
		super.onAttach( activity );

		if( activity instanceof PurchaseScreenProvider )
		{
			m_purchaseScreenProvider = (PurchaseScreenProvider) activity;
		}
	}

	@Override
	public void onDetach()
	{
		super.onDetach();

		m_purchaseScreenProvider = null;
	}

	@Override
	public void onCreate( final Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		Bundle args = getArguments();
		if( args != null )
		{
			m_tip = (Tips.Tip) args.getSerializable( ARG_TIP );
		}
	}

	@Override
	public View onCreateView( final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState )
	{
		View view = null;

		if( Tips.Tip.TYPE_TIP.equalsIgnoreCase( m_tip.type ) )
		{
			view = inflater.inflate( R.layout.tip_fragment, container, false );
		}
		else if( Tips.Tip.TYPE_PRO.equalsIgnoreCase( m_tip.type ) )
		{
			view = inflater.inflate( R.layout.tip_pro_fragment, container, false );

			Button proButton = (Button) view.findViewById( R.id.TIP_pro_button );
			proButton.setOnClickListener( this );
		}
		else if( Tips.Tip.TYPE_RATE.equalsIgnoreCase( m_tip.type ) )
		{
			view = inflater.inflate( R.layout.tip_rate_fragment, container, false );

			Button rateButton = (Button) view.findViewById( R.id.TIP_rate_button );
			rateButton.setOnClickListener( this );
		}

		Dialog dialog = getDialog();
		if( dialog != null )
		{
			dialog.setTitle( m_tip.title );
		}

		TextView bodyView = (TextView) view.findViewById( R.id.TIP_body );
		bodyView.setText( m_tip.body );

		return view;
	}

	@Override
	public void onResume()
	{
		super.onResume();

		Tips.recordTipViewed( m_tip, getActivity() );
	}

	@Override
	public void onClick( final View v )
	{
		if( v.getId() == R.id.TIP_pro_button )
		{
			if( m_purchaseScreenProvider != null )
			{
				m_purchaseScreenProvider.showPurchaseScreen();
			}
		}
		else if( v.getId() == R.id.TIP_rate_button )
		{
			Intent intent = new Intent( Intent.ACTION_VIEW );
			intent.setData( Uri.parse( "market://details?id=com.darkrockstudios.apps.randdit" ) );
			startActivity( intent );
		}

		dismiss();
	}
}
