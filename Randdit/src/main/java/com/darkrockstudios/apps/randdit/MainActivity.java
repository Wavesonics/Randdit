package com.darkrockstudios.apps.randdit;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
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
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.darkrockstudios.apps.randdit.fragments.AboutFragment;
import com.darkrockstudios.apps.randdit.fragments.IntroFragment;
import com.darkrockstudios.apps.randdit.fragments.PostFragment;
import com.darkrockstudios.apps.randdit.fragments.PurchaseProFragment;
import com.darkrockstudios.apps.randdit.googleplaygames.GameHelper;
import com.darkrockstudios.apps.randdit.misc.Analytics;
import com.darkrockstudios.apps.randdit.misc.Categories;
import com.darkrockstudios.apps.randdit.misc.CategoryDefinition;
import com.darkrockstudios.apps.randdit.misc.CategoryUtility;
import com.darkrockstudios.apps.randdit.misc.NavDrawerAdapter;
import com.darkrockstudios.apps.randdit.misc.NextButtonEnabler;
import com.darkrockstudios.apps.randdit.misc.OsUtils;
import com.darkrockstudios.apps.randdit.misc.Post;
import com.darkrockstudios.apps.randdit.misc.Preferences;
import com.darkrockstudios.apps.randdit.misc.PurchaseScreenProvider;
import com.darkrockstudios.apps.randdit.misc.StatCounter;
import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.games.Games;
import com.google.gson.Gson;
import com.google.sample.castcompanionlibrary.cast.BaseCastManager;
import com.google.sample.castcompanionlibrary.cast.DataCastManager;
import com.google.sample.castcompanionlibrary.cast.callbacks.DataCastConsumerImpl;
import com.google.sample.castcompanionlibrary.cast.exceptions.NoConnectionException;
import com.google.sample.castcompanionlibrary.cast.exceptions.TransientNetworkDisconnectionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class MainActivity extends NavDrawerActivity implements BillingActivity.ProStatusListener, PurchaseScreenProvider
{
	private static final String TAG = MainActivity.class.getSimpleName();

	private static final String CONTENT_FRAGMENT_TAG  = "ContentFragment";
	private static final String PURCHASE_FRAGMENT_TAG = "PurchaseFragment";
	private static final String ABOUT_FRAGMENT_TAG    = "AboutFragment";

	private static final String GOOGLE_CAST_DATA_NAMESPACE = "urn:x-cast:com.darkrockstudios.apps.randdit";

	private static final String SAVE_POSTS    = MainActivity.class.getName() + ".POSTS";
	private static final String SAVE_NAV_ITEM = MainActivity.class.getName() + ".NAV_ITEM";

	private static final int REQUEST_LEADERBOARD  = 31;
	private static final int REQUEST_ACHIEVEMENTS = 42;

	private LinkedList<Post> m_posts;

	private NfcAdapter  m_nfcAdapter;
	private AlertDialog m_wifiAlert;

	private static DataCastManager  m_castManager;
	private        DataCastConsumer m_dataConsumer;

	private NavDrawerAdapter.NavItem m_currentCategory;

	private Categories m_categories;

	private boolean m_isActive;

	private MenuItem m_loadingItem;

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
		supportRequestWindowFeature( Window.FEATURE_INDETERMINATE_PROGRESS );
		setProStatusListener( this );

		super.onCreate( savedInstanceState );

		PreferenceManager.setDefaultValues( this, R.xml.settings_pro, false );

		initNfc();

		if( BuildConfig.DEBUG )
		{
			initGoogleCast();
		}

		m_posts = new LinkedList<>();

		m_categories = CategoryUtility.getCategories( this );
		if( m_categories == null )
		{
			requestCategories( false );
		}
		else
		{
			m_navDrawerAdapter.setCategories( m_categories );
		}

		m_navDrawerAdapter.setGameHelper( m_gameHelper );
		m_navDrawerAdapter.setSignedIn( m_gameHelper.isSignedIn() );

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
			IntroFragment fragment = IntroFragment.newInstance( isPro() );

			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction().replace( R.id.content_frame, fragment, CONTENT_FRAGMENT_TAG ).commit();
		}
		else
		{
			requestPost( postId );
		}
	}

	private void requestCategories( final boolean showPostOnUpdate )
	{
		setSupportProgressBarIndeterminateVisibility( true );
		setNextImageButtonEnabled( false );

		final String url = "http://randdit.com/actions/get_default_categories.php";
		CategoriesHandler responseHandler = new CategoriesHandler( showPostOnUpdate );
		JsonObjectRequest jsObjRequest = new JsonObjectRequest( url, null, responseHandler, responseHandler );
		jsObjRequest.setRetryPolicy( new CustomRetryPolicy() );

		RequestQueue requestQueue = RandditApplication.getRequestQueue();
		requestQueue.add( jsObjRequest );
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
				if( pathSegments != null && pathSegments.size() > 1 && m_categories != null )
				{
					// We have a post at the point, so set our category
					CategoryDefinition category = CategoryUtility.findByName( pathSegments.get( 0 ), m_categories );
					if( category != null )
					{
						updateCategory( new NavDrawerAdapter.NavItem( category ) );
					}
					else
					{
						updateCategory( NavDrawerAdapter.CATEGORY_ALL );
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
		getMenuInflater().inflate( R.menu.main, menu );

		m_loadingItem = menu.findItem( R.id.menu_item_loading );
		if( m_loadingItem != null )
		{
			m_loadingItem.setEnabled( false );
			m_loadingItem.setActionView( R.layout.menu_item_loading );
			m_loadingItem.expandActionView();
		}

		if( BuildConfig.DEBUG )
		{
			getDataCastManager( this ).addMediaRouterButton( menu, R.id.media_route_menu_item );
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
			if( id == R.id.menu_item_settings )
			{
				Intent intent = new Intent( this, SettingsActivity.class );
				startActivity( intent );

				Analytics.trackSettings( isPro() );

				handled = true;
			}
			else if( id == R.id.menu_item_share )
			{
				Analytics.trackShare( m_currentCategory, isPro() );
				handled = super.onOptionsItemSelected( item );
			}
			else if( id == R.id.menu_item_about )
			{
				Analytics.trackAboutClick( m_currentCategory );

				AboutFragment fragment = AboutFragment.newInstance();
				fragment.show( getFragmentManager(), ABOUT_FRAGMENT_TAG );

				handled = true;
			}
			else if( id == R.id.menu_item_loading )
			{
				return true;
			}
			else
			{
				handled = false;
			}
		}
		return handled;
	}

	@Override
	public void onStart()
	{
		super.onStart();
	}

	@Override
	public void onStop()
	{
		super.onStop();

		submitScores();
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

		if( BuildConfig.DEBUG )
		{
			getDataCastManager( this );
		}

		beginUserInitiatedSignIn();
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

	@Override
	public void setSupportProgressBarIndeterminateVisibility( boolean visible )
	{
		// TODO: Dirty hack for 5.0 devices for now
		if( !OsUtils.atLeastLollipop() )
		{
			super.setSupportProgressBarIndeterminateVisibility( visible );
		}
		else if( m_loadingItem != null )
		{
			m_loadingItem.setVisible( visible );
		}
	}

	private void updateCategory( final NavDrawerAdapter.NavItem category )
	{
		m_currentCategory = category;
		setTitle();
	}

	private void requestPost( final String postId )
	{
		if( m_currentCategory != null )
		{
			setSupportProgressBarIndeterminateVisibility( true );
			setNextImageButtonEnabled( false );

			final String url = "http://randdit.com/" + NavDrawerAdapter.getId( m_currentCategory ) + "/" + postId + "/?api";
			Log.d( TAG, "Requestion: " + url );
			RandditPostHandler responseHandler = new RandditPostHandler();
			JsonObjectRequest jsObjRequest =
					new JsonObjectRequest( url, null, responseHandler, responseHandler );
			jsObjRequest.setRetryPolicy( new CustomRetryPolicy() );

			RequestQueue requestQueue = RandditApplication.getRequestQueue();
			requestQueue.add( jsObjRequest );
		}
	}

	private void requestPosts()
	{
		if( m_currentCategory != null )
		{
			setSupportProgressBarIndeterminateVisibility( true );
			setNextImageButtonEnabled( false );

			final String url = "http://randdit.com/" + NavDrawerAdapter.getId( m_currentCategory ) + "/?api";
			RandditPostHandler responseHandler = new RandditPostHandler();
			JsonObjectRequest jsObjRequest =
					new JsonObjectRequest( url, null, responseHandler, responseHandler );
			jsObjRequest.setRetryPolicy( new CustomRetryPolicy() );

			RequestQueue requestQueue = RandditApplication.getRequestQueue();
			requestQueue.add( jsObjRequest );
		}
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
		// Try to request categories if we have failed to get them by this point
		if( m_categories == null )
		{
			requestCategories( true );
		}
		else
		{
			if( m_currentCategory == null )
			{
				updateCategory( NavDrawerAdapter.CATEGORY_ALL );
			}

			Analytics.trackNextImageClick( m_currentCategory, isPro() );
			showPost();
		}
	}

	private Post getPost()
	{
		Post post = null;

		// Keep popping posts until we find an Image post
		while( m_posts.size() > 0 && post == null )
		{
			Post nextPost = m_posts.pop();
			if( Post.isPostValid( nextPost ) && nextPost.is_image == 1 )
			{
				post = nextPost;
			}
		}

		return post;
	}

	private void showPost()
	{
		if( m_currentCategory != null )
		{
			Post post = getPost();
			if( post != null )
			{
				if( m_isActive )
				{
					FragmentManager fragmentManager = getFragmentManager();

					sendCastUpdate( post );

					PostFragment fragment = PostFragment.newInstance( post, isPro(), m_currentCategory );
					fragmentManager.beginTransaction().replace( R.id.content_frame, fragment, CONTENT_FRAGMENT_TAG ).commit();

					updateNfcMessage( post );

					// Unlock some achievements!
					checkAchievements();
				}
			}
			else
			{
				requestPosts();
			}
		}
	}

	private void sendCastUpdate( final Post post )
	{
		DataCastManager dataCastManager = getDataCastManager( this );
		if( dataCastManager.isConnected() )
		{
			try
			{
				Gson gson = new Gson();
				String message = gson.toJson( post );
				dataCastManager.sendDataMessage( message, GOOGLE_CAST_DATA_NAMESPACE );
			}
			catch( IOException | TransientNetworkDisconnectionException | NoConnectionException e )
			{
				e.printStackTrace();
			}
		}
	}

	private void checkAchievements()
	{
		if( isSignedIn() )
		{
			long views = StatCounter.getImageViewCount( this );
			if( views >= 100 )
			{
				Log.i( TAG, "Unlocking 100 Views Achievement" );
				Games.Achievements.unlock( getApiClient(), getString( R.string.achievement_100_views ) );
			}

			if( views >= 500 )
			{
				Log.i( TAG, "Unlocking 500 Views Achievement" );
				Games.Achievements.unlock( getApiClient(), getString( R.string.achievement_500_views ) );
			}

			if( views >= 1000 )
			{
				Log.i( TAG, "Unlocking 1000 Views Achievement" );
				Games.Achievements.unlock( getApiClient(), getString( R.string.achievement_1000_views ) );
			}

			if( views >= 2000 )
			{
				Log.i( TAG, "Unlocking 2000 Views Achievement" );
				Games.Achievements.unlock( getApiClient(), getString( R.string.achievement_2000_views ) );
			}

			if( views >= 5000 )
			{
				Log.i( TAG, "Unlocking 5000 Views Achievement" );
				Games.Achievements.unlock( getApiClient(), getString( R.string.achievement_5000_views ) );
			}
		}
	}

	@Override
	public void onItemClick( final AdapterView<?> parent, final View view, final int position, final long id )
	{
		NavDrawerAdapter.NavItem navItem = m_navDrawerAdapter.getItem( position );
		if( navItem.isCategory )
		{
			m_currentCategory = navItem;
			m_posts.clear();
			requestPosts();

			Analytics.trackCategoryChange( m_currentCategory, isPro() );
		}
		else
		{
			switch( navItem.type )
			{
				case ProAd:
					Analytics.trackProClick( "nav_drawer" );

					showPurchaseScreen();
					break;
				case Leaderboards:
					m_drawerLayout.closeDrawer( Gravity.LEFT );

					submitScores();

					startActivityForResult( Games.Leaderboards.getLeaderboardIntent( getApiClient(),
					                                                                 getString( R.string.leaderboard_views ) ),
					                        REQUEST_LEADERBOARD );

					break;
				case Achievements:
					m_drawerLayout.closeDrawer( Gravity.LEFT );

					startActivityForResult( Games.Achievements.getAchievementsIntent( getApiClient() ), REQUEST_ACHIEVEMENTS );
					break;
			}
		}

		m_drawerLayout.closeDrawer( m_navDrawerView );
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

		getSupportActionBar().setTitle( newTitle );
	}

	public static DataCastManager getDataCastManager( final Context ctx )
	{
		if( m_castManager == null )
		{
			m_castManager = DataCastManager.initialize( ctx,
			                                            ctx.getString( R.string.google_cast_app_id ),
			                                            GOOGLE_CAST_DATA_NAMESPACE );
			m_castManager.enableFeatures( DataCastManager.FEATURE_NOTIFICATION |
			                              DataCastManager.FEATURE_LOCKSCREEN |
			                              DataCastManager.FEATURE_DEBUGGING );
		}

		m_castManager.setContext( ctx );

		return m_castManager;
	}

	private void initNfc()
	{
		m_nfcAdapter = NfcAdapter.getDefaultAdapter( this );
		if( m_nfcAdapter == null )
		{
			Log.i( TAG, "NFC not available. Android Beam functionality disabled." );
		}
	}

	private void initGoogleCast()
	{
		BaseCastManager.checkGooglePlaySevices( this );

		m_dataConsumer = new DataCastConsumer();
		DataCastManager dataCastManager = getDataCastManager( this );
		dataCastManager.addDataCastConsumer( m_dataConsumer );
		dataCastManager.reconnectSessionIfPossible( this, true );
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
		runOnUiThread( new Runnable()
		{
			@Override
			public void run()
			{
				m_navDrawerAdapter.setPro( isPro() );
				m_navDrawerAdapter.refreshNavItems();
			}
		} );

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

	@Override
	public void onSignInFailed()
	{
		Log.d( TAG, "Google Games onSignInFailed" );

		if( hasSignInError() )
		{
			GameHelper.SignInFailureReason reason = getSignInError();
			Log.d( TAG, "ServiceError: " + reason );
		}
		else
		{
			Log.d( TAG, "No sign in error available" );
		}

		m_navDrawerAdapter.setSignedIn( false );
		m_navDrawerAdapter.refreshNavItems();

		Crouton.makeText( this, R.string.toast_signin_failed, Style.ALERT );
	}

	@Override
	public void onSignInSucceeded()
	{
		Log.d( TAG, "Google Games onSignInSucceeded" );

		m_navDrawerAdapter.setSignedIn( true );
		m_navDrawerAdapter.refreshNavItems();

		Crouton.makeText( this, R.string.toast_signin_success, Style.CONFIRM );
	}

	private void submitScores()
	{
		if( isSignedIn() )
		{
			final long allTimeViews = StatCounter.getImageViewCount( this );
			if( allTimeViews > 0 )
			{
				Log.d( TAG, "Submitting all time view score: " + allTimeViews );
				Games.Leaderboards.submitScore( getApiClient(), getString( R.string.leaderboard_views ), allTimeViews );
			}
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
			catch( final JSONException e )
			{
				e.printStackTrace();
			}
			finally
			{
				setSupportProgressBarIndeterminateVisibility( false );
				setNextImageButtonEnabled( true );
				if( m_posts.size() > 0 )
				{
					showPost();
				}
				else
				{
					Crouton.makeText( MainActivity.this, R.string.toast_post_request_error, Style.ALERT ).show();
				}
			}
		}

		@Override
		public void onErrorResponse( final VolleyError volleyError )
		{
			Log.d( TAG, "Failed to retrieve posts." );
			Log.d( TAG, volleyError.toString() );

			Crouton.makeText( MainActivity.this, R.string.toast_post_request_error, Style.ALERT ).show();

			setSupportProgressBarIndeterminateVisibility( false );
			setNextImageButtonEnabled( true );
		}
	}

	private class CategoriesHandler implements Response.Listener<JSONObject>, Response.ErrorListener
	{
		private final boolean m_getPostOnUpdate;

		public CategoriesHandler( final boolean getPostOnUpdate )
		{
			m_getPostOnUpdate = getPostOnUpdate;
		}

		@Override
		public void onErrorResponse( final VolleyError volleyError )
		{
			Log.d( TAG, "Failed to retrieve categories." );
			Log.d( TAG, volleyError.toString() );

			Crouton.makeText( MainActivity.this, R.string.toast_categories_failure, Style.ALERT ).show();

			setSupportProgressBarIndeterminateVisibility( false );
			setNextImageButtonEnabled( true );
		}

		@Override
		public void onResponse( final JSONObject jsonObject )
		{
			String categoriesStr = jsonObject.toString();
			Gson gson = new Gson();
			Categories categories = gson.fromJson( categoriesStr, Categories.class );
			CategoryUtility.writeFile( categories, MainActivity.this );

			m_categories = categories;
			m_navDrawerAdapter.setCategories( m_categories );

			setSupportProgressBarIndeterminateVisibility( false );
			setNextImageButtonEnabled( true );

			Crouton.makeText( MainActivity.this, R.string.toast_categories_success, Style.INFO ).show();

			if( m_currentCategory == null )
			{
				updateCategory( NavDrawerAdapter.CATEGORY_ALL );
			}

			if( m_getPostOnUpdate )
			{
				showPost();
			}
		}
	}

	private final class DataCastConsumer extends DataCastConsumerImpl
	{
		@Override
		public void onMessageReceived( final CastDevice castDevice,
		                               final String namespace,
		                               final String message )
		{
			Toast.makeText( MainActivity.this, "Cast msg received", Toast.LENGTH_LONG ).show();
		}

		@Override
		public void onMessageSendFailed( final Status status )
		{
			Toast.makeText( MainActivity.this, "Cast msg failed: " + status.getStatusCode(), Toast.LENGTH_LONG ).show();
		}

		@Override
		public void onApplicationDisconnected( final int errorCode )
		{
			Toast.makeText( MainActivity.this, "Device dissconnected " + errorCode, Toast.LENGTH_LONG ).show();
		}

		@Override
		public void onApplicationConnected( final ApplicationMetadata appMetadata, final String applicationStatus, final String sessionId, final boolean wasLaunched )
		{

		}
	}
}
