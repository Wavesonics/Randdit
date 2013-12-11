package com.darkrockstudios.apps.randdit;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.darkrockstudios.apps.randdit.fragments.IntroFragment;
import com.darkrockstudios.apps.randdit.fragments.PostFragment;
import com.darkrockstudios.apps.randdit.fragments.PurchaseProFragment;
import com.darkrockstudios.apps.randdit.misc.Analytics;
import com.darkrockstudios.apps.randdit.misc.NavDrawerAdapter;
import com.darkrockstudios.apps.randdit.misc.NextButtonEnabler;
import com.darkrockstudios.apps.randdit.misc.Post;
import com.darkrockstudios.apps.randdit.misc.Preferences;
import com.darkrockstudios.apps.randdit.misc.PurchaseScreenProvider;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class MainActivity extends NavDrawerActivity implements BillingActivity.ProStatusListener, PurchaseScreenProvider
{
	private static final String TAG = MainActivity.class.getSimpleName();

	private static final String CONTENT_FRAGMENT_TAG  = "ContentFragment";
	private static final String PURCHASE_FRAGMENT_TAG = "PurchaseFragment";

	private static final String SAVE_POSTS    = MainActivity.class.getName() + ".POSTS";
	private static final String SAVE_NAV_ITEM = MainActivity.class.getName() + ".NAV_ITEM";

	private LinkedList<Post> m_posts;

	private NfcAdapter  m_nfcAdapter;
	private AlertDialog m_wifiAlert;

	private NavDrawerAdapter.NavItem m_currentCategory;

	private boolean m_isActive;

	@Override
	public void showPurchaseScreen()
	{
		FragmentManager fragmentManager = getFragmentManager();
		PurchaseProFragment fragment = PurchaseProFragment.newInstance();
		fragment.show( fragmentManager, PURCHASE_FRAGMENT_TAG );
	}

	@Override
	protected void onCreate( final Bundle savedInstanceState )
	{
		setProStatusListener( this );

		super.onCreate( savedInstanceState );

		// Don't report starts during testing
		if( BuildConfig.DEBUG )
		{
			GoogleAnalytics.getInstance( this ).setDryRun( true );
		}

		PreferenceManager.setDefaultValues( this, R.xml.settings, false );

		initNfc();

		m_posts = new LinkedList<>();

		final String postId = getPostFromIntent( getIntent() );
		if( savedInstanceState != null )
		{
			if( savedInstanceState.containsKey( SAVE_POSTS ) )
			{
				List<Post> postList = (List<Post>) savedInstanceState.getSerializable( SAVE_POSTS );
				m_posts.addAll( postList );
			}

			if( savedInstanceState.containsKey( SAVE_NAV_ITEM ) )
			{
				m_currentCategory = (NavDrawerAdapter.NavItem) savedInstanceState.getSerializable( SAVE_NAV_ITEM );
				setTitle();
			}
		}
		else if( postId == null )
		{
			IntroFragment fragment = IntroFragment.newInstance();

			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction().replace( R.id.content_frame, fragment, CONTENT_FRAGMENT_TAG ).commit();
		}
		else
		{
			requestPost( postId );
		}
	}

	private String getPostFromIntent( final Intent intent )
	{
		String postId = null;

		if( intent != null )
		{
			if( (Intent.ACTION_VIEW.equals( intent.getAction() ) ||
			     NfcAdapter.ACTION_NDEF_DISCOVERED.equals( intent.getAction() ))
			    && intent.getData() != null )
			{
				Uri uri = intent.getData();
				List<String> pathSegments = uri.getPathSegments();
				if( pathSegments != null && pathSegments.size() > 1 )
				{
					// We have a post at the point, so set our category
					try
					{
						updateCategory( NavDrawerAdapter.NavItem.valueOf( pathSegments.get( 0 ) ) );
					}
					catch( IllegalArgumentException e )
					{
						updateCategory( NavDrawerAdapter.NavItem.all );
					}

					postId = pathSegments.get( 1 );
				}
			}
		}

		return postId;
	}

	protected void onSaveInstanceState( final Bundle outState )
	{
		super.onSaveInstanceState( outState );

		outState.putSerializable( SAVE_POSTS, m_posts );
		outState.putSerializable( SAVE_NAV_ITEM, m_currentCategory );
	}

	@Override
	public boolean onCreateOptionsMenu( final Menu menu )
	{
		if( isPro() )
		{
			getMenuInflater().inflate( R.menu.main_pro, menu );
		}
		else
		{
			getMenuInflater().inflate( R.menu.main, menu );
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected( final MenuItem item )
	{
		final boolean handled;
		if( super.onOptionsItemSelected( item ) )
		{
			handled = true;
		}
		else
		{
			int id = item.getItemId();
			if( id == R.id.action_settings )
			{
				handled = true;

				Intent intent = new Intent( this, SettingsActivity.class );
				startActivity( intent );

				Analytics.trackSettings( this );
			}
			else if( id == R.id.menu_item_share )
			{
				Analytics.trackShare( this, m_currentCategory );
				handled = super.onOptionsItemSelected( item );
			}
			else if( id == R.id.action_purchase_pro )
			{
				showPurchaseScreen();

				handled = true;
			}
			else
			{
				handled = super.onOptionsItemSelected( item );
			}
		}
		return handled;
	}

	@Override
	public void onStart()
	{
		super.onStart();

		EasyTracker.getInstance( this ).activityStart( this );
	}

	@Override
	public void onStop()
	{
		super.onStop();

		EasyTracker.getInstance( this ).activityStop( this );
	}

	@Override
	public void onResume()
	{
		super.onResume();

		m_isActive = true;

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( this );
		if( settings.getBoolean( Preferences.KEY_KEEP_SCREEN_ON, true ) )
		{
			getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
		}
		else
		{
			getWindow().clearFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
		}

		// Refresh the adapter to accommodate changes to what is shown
		m_navDrawerAdapter.refreshNavItems();

		if( shouldShowWifiWarning() )
		{
			presentDataWarning();
		}
	}

	@Override
	public void onPause()
	{
		super.onPause();

		m_isActive = false;

		if( m_wifiAlert != null )
		{
			m_wifiAlert.dismiss();
			m_wifiAlert = null;
		}
	}

	private void updateCategory( final NavDrawerAdapter.NavItem category )
	{
		m_currentCategory = category;
		setTitle();
	}

	private void requestPost( final String postId )
	{
		setProgressBarIndeterminateVisibility( true );
		setNextImageButtonEnabled( false );

		final String url = "http://randdit.com/" + NavDrawerAdapter.getId( m_currentCategory ) + "/" + postId + "/?api";
		RandditPostHandler responseHandler = new RandditPostHandler();
		JsonObjectRequest jsObjRequest =
				new JsonObjectRequest( url, null, responseHandler, responseHandler );

		RequestQueue requestQueue = RandditApplication.getRequestQueue();
		requestQueue.add( jsObjRequest );
	}

	private void requestPosts()
	{
		setProgressBarIndeterminateVisibility( true );
		setNextImageButtonEnabled( false );

		final String url = "http://randdit.com/" + NavDrawerAdapter.getId( m_currentCategory ) + "/?api";
		RandditPostHandler responseHandler = new RandditPostHandler();
		JsonObjectRequest jsObjRequest =
				new JsonObjectRequest( url, null, responseHandler, responseHandler );

		RequestQueue requestQueue = RandditApplication.getRequestQueue();
		requestQueue.add( jsObjRequest );
	}

	private void setNextImageButtonEnabled( final boolean enabled )
	{
		if( m_isActive )
		{
			FragmentManager fragmentManager = getFragmentManager();
			Fragment fragment = fragmentManager.findFragmentByTag( CONTENT_FRAGMENT_TAG );
			if( fragment != null && fragment instanceof NextButtonEnabler )
			{
				NextButtonEnabler buttonEnabler = (NextButtonEnabler) fragment;
				buttonEnabler.setNextButtonEnabled( enabled );
			}
		}
	}

	public void onShowImageClicked( final View view )
	{
		if( m_currentCategory == null )
		{
			updateCategory( NavDrawerAdapter.NavItem.all );
		}

		Analytics.trackNextImageClick( this, m_currentCategory );
		showPost();
	}

	private void showPost()
	{
		Post post = (m_posts.size() > 0 ? m_posts.pop() : null);
		if( post != null )
		{
			if( m_isActive )
			{
				FragmentManager fragmentManager = getFragmentManager();

				PostFragment fragment = PostFragment.newInstance( post, isPro(), m_currentCategory );
				fragmentManager.beginTransaction().replace( R.id.content_frame, fragment, CONTENT_FRAGMENT_TAG ).commit();

				updateNfcMessage( post );
			}
		}
		else
		{
			requestPosts();
		}
	}

	@Override
	public void onItemClick( final AdapterView<?> parent, final View view, final int position, final long id )
	{
		m_currentCategory = m_navDrawerAdapter.getItem( position );
		m_posts.clear();
		requestPosts();
		m_drawerLayout.closeDrawer( m_navDrawerView );

		Analytics.trackCategoryChange( this, m_currentCategory );
	}

	@Override
	public void setTitle()
	{
		String appName = getString( R.string.app_name );

		final String newTitle;
		if( m_currentCategory != null )
		{
			String sectionTitle = NavDrawerAdapter.getId( m_currentCategory );
			newTitle = appName + " - " + sectionTitle;
		}
		else
		{
			newTitle = appName;
		}

		getActionBar().setTitle( newTitle );
	}

	private void initNfc()
	{
		m_nfcAdapter = NfcAdapter.getDefaultAdapter( this );
		if( m_nfcAdapter == null )
		{
			Log.i( TAG, "NFC not available. Android Beam functionality disabled." );
		}
	}

	private void updateNfcMessage( final Post post )
	{
		if( m_nfcAdapter != null )
		{
			NdefRecord record = NdefRecord.createUri( PostFragment.createRandditUrl( post, m_currentCategory ) );
			NdefRecord[] records = new NdefRecord[ 1 ];
			records[ 0 ] = record;
			NdefMessage message = new NdefMessage( records );
			m_nfcAdapter.setNdefPushMessage( message, this );
		}
	}

	private boolean isOnWifi()
	{
		final boolean onWifi;

		ConnectivityManager connManager = (ConnectivityManager) getSystemService( CONNECTIVITY_SERVICE );
		NetworkInfo wifi = connManager.getNetworkInfo( ConnectivityManager.TYPE_WIFI );

		if( wifi != null && wifi.isConnected() )
		{
			onWifi = true;
		}
		else
		{
			onWifi = false;
		}

		return onWifi;
	}

	private void presentDataWarning()
	{
		if( m_wifiAlert == null )
		{
			WifiDialogListener listener = new WifiDialogListener();

			AlertDialog.Builder builder = new AlertDialog.Builder( this );
			builder.setTitle( "Data usage warning!" );
			builder.setIcon( R.drawable.ic_action_bars );
			builder.setMessage( Html.fromHtml( "<strong>It looks like you're not on Wifi.</strong><br /><br />Randdit can end up using a ton of data (<em>some of those Doge GIFs are HUGE!</em>). So be careful and make sure you aren't blowing through your data plan." ) );
			builder.setNegativeButton( "Ok", null );
			builder.setPositiveButton( "Ok, don't warn me again", listener );
			m_wifiAlert = builder.create();
			m_wifiAlert.show();
		}
	}

	private boolean shouldShowWifiWarning()
	{
		final boolean shouldShow;

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( MainActivity.this );
		boolean showWifiWarning = settings.getBoolean( Preferences.KEY_SHOW_WIFI_WARNING, true );
		if( showWifiWarning && !isOnWifi() )
		{
			shouldShow = true;
		}
		else
		{
			shouldShow = false;
		}

		return shouldShow;
	}

	@Override
	public void onProStatusUpdate( final boolean isPro )
	{
		if( isPro )
		{
			// If we are already viewing a post, we wan't to release while still viewing that post
			Fragment fragment = getFragmentManager().findFragmentByTag( CONTENT_FRAGMENT_TAG );
			if( fragment != null && fragment instanceof PostFragment )
			{
				PostFragment postFragment = (PostFragment) fragment;
				Post currentPost = postFragment.getPost();

				String url = PostFragment.createRandditUrl( currentPost, m_currentCategory );
				Uri uri = Uri.parse( url );

				Intent intent = new Intent( this, MainActivity.class );
				intent.setAction( Intent.ACTION_VIEW );
				intent.setData( uri );
				startActivity( intent );
			}
			else
			{
				Intent intent = new Intent( this, MainActivity.class );
				startActivity( intent );
			}
			finish();
		}
	}

	private class WifiDialogListener implements Dialog.OnClickListener
	{
		@Override
		public void onClick( final DialogInterface dialog, final int which )
		{
			if( which == DialogInterface.BUTTON_POSITIVE )
			{
				SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( MainActivity.this );
				SharedPreferences.Editor editor = settings.edit();
				editor.putBoolean( Preferences.KEY_SHOW_WIFI_WARNING, false );
				editor.apply();
			}
		}
	}

	private class RandditPostHandler implements Response.Listener<JSONObject>, Response.ErrorListener
	{
		public RandditPostHandler()
		{

		}

		@Override
		public void onResponse( final JSONObject jsonObject )
		{
			Log.d( TAG, "Posts received." );

			try
			{
				JSONArray posts = jsonObject.getJSONArray( "posts" );
				for( int ii = 0; ii < posts.length(); ++ii )
				{
					JSONObject obj = posts.getJSONObject( ii );

					Gson gson = new Gson();
					Post post = gson.fromJson( obj.toString(), Post.class );

					if( post != null )
					{
						m_posts.add( post );
					}
				}
			}
			catch( JSONException e )
			{
				e.printStackTrace();
			}
			finally
			{
				setProgressBarIndeterminateVisibility( false );
				setNextImageButtonEnabled( true );
				showPost();
			}
		}

		@Override
		public void onErrorResponse( final VolleyError volleyError )
		{
			Log.d( TAG, "Failed to retrieve posts." );
			Log.d( TAG, volleyError.toString() );

			setProgressBarIndeterminateVisibility( false );
			setNextImageButtonEnabled( true );
		}
	}
}
