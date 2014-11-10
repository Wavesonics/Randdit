package com.darkrockstudios.apps.randdit.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.darkrockstudios.apps.randdit.R;
import com.darkrockstudios.apps.randdit.misc.Analytics;


public class AboutFragment extends DialogFragment implements View.OnClickListener
{

	public static AboutFragment newInstance()
	{
		AboutFragment fragment = new AboutFragment();

		return fragment;
	}

	public AboutFragment()
	{

	}

	@Override
	public void onCreate( final Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
	}

	@Override
	public View onCreateView( final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState )
	{
		View view = inflater.inflate( R.layout.fragment_about, container, false );

		Dialog dialog = getDialog();
		if( dialog != null )
		{
			dialog.setTitle( R.string.about_title );
		}

		Button marketButton = (Button) view.findViewById( R.id.ABOUT_market_button );
		marketButton.setOnClickListener( this );

		MovementMethod linkMovementMethod = LinkMovementMethod.getInstance();

		TextView linkView = (TextView) view.findViewById( R.id.ABOUT_github_giflib );
		linkView.setMovementMethod( linkMovementMethod );
		linkView.setText( getText( R.string.about_body_github_giflib ) );

		linkView = (TextView) view.findViewById( R.id.ABOUT_github_photoview );
		linkView.setMovementMethod( linkMovementMethod );
		linkView.setText( getText( R.string.about_body_github_photoview ) );

		linkView = (TextView) view.findViewById( R.id.ABOUT_feedback );
		linkView.setMovementMethod( linkMovementMethod );
		linkView.setText( getText( R.string.about_body_feedback ) );

		TextView versionView = (TextView) view.findViewById( R.id.ABOUT_app_version );
		versionView.setText( "v" + getAppVersion() );

		return view;
	}

	private String getAppVersion()
	{
		String version = "-";

		Activity activity = getActivity();
		if( activity != null )
		{
			try
			{
				PackageManager pm = activity.getPackageManager();
				if( pm != null )
				{
					PackageInfo pinfo = pm.getPackageInfo( activity.getPackageName(), 0 );
					version = pinfo.versionName;
				}
			}
			catch( PackageManager.NameNotFoundException e )
			{
				e.printStackTrace();
			}
		}

		return version;
	}

	@Override
	public void onClick( final View v )
	{
		if( v.getId() == R.id.ABOUT_market_button && isAdded() )
		{
			Analytics.trackViewOtherAppsClick();

			Intent intent = new Intent( Intent.ACTION_VIEW );
			intent.setData( Uri.parse( "market://search?q=pub:Dark+Rock+Studios" ) );
			startActivity( intent );
		}
	}
}
