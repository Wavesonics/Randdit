package com.darkrockstudios.apps.randdit.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.darkrockstudios.apps.randdit.R;
import com.darkrockstudios.apps.randdit.misc.Analytics;
import com.darkrockstudios.apps.randdit.misc.PurchaseProvider;

/**
 * Created by Adam on 12/10/13.
 */
public class PurchaseProFragment extends DialogFragment implements View.OnClickListener
{
	private PurchaseProvider m_purchaseProvider;

	public static PurchaseProFragment newInstance()
	{
		return new PurchaseProFragment();
	}

	@Override
	public void onAttach( final Activity activity )
	{
		super.onAttach( activity );

		if( activity instanceof PurchaseProvider )
		{
			m_purchaseProvider = (PurchaseProvider) activity;
		}
	}

	@Override
	public void onDetach()
	{
		super.onDetach();

		m_purchaseProvider = null;
	}

	@Override
	public void onCreate( final Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		Analytics.trackScreen( getActivity(), PurchaseProFragment.class.getSimpleName(), false );
	}

	@Override
	public View onCreateView( final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState )
	{
		View view = inflater.inflate( R.layout.purchase_pro, container, false );

		Button purchaseButton = (Button) view.findViewById( R.id.PRO_purchase_button );
		purchaseButton.setOnClickListener( this );

		Dialog dialog = getDialog();
		if( dialog != null )
		{
			dialog.setTitle( R.string.PRO_title );
		}

		return view;
	}

	@Override
	public void onClick( final View v )
	{
		if( v.getId() == R.id.PRO_purchase_button && m_purchaseProvider != null )
		{
			m_purchaseProvider.purchasePro();
			dismiss();
		}
	}
}
