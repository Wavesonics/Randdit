package com.darkrockstudios.apps.randdit;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.darkrockstudios.apps.randdit.fragments.IntroFragment;
import com.darkrockstudios.apps.randdit.fragments.PostFragment;
import com.darkrockstudios.apps.randdit.misc.Analytics;
import com.darkrockstudios.apps.randdit.misc.NavDrawerAdapter;
import com.darkrockstudios.apps.randdit.misc.Post;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class MainActivity extends Activity implements AdapterView.OnItemClickListener
{
	private static final String TAG = MainActivity.class.getSimpleName();

	private static final String CONTENT_FRAGMENT_TAG = "ContentFragment";

	private static final String SAVE_POSTS    = MainActivity.class.getName() + ".POSTS";
	private static final String SAVE_NAV_ITEM = MainActivity.class.getName() + ".NAV_ITEM";

	private DrawerToggle m_drawerToggle;
	private DrawerLayout m_drawerLayout;
	private ListView     m_navDrawerView;

	private NavDrawerAdapter m_navDrawerAdapter;
	private LinkedList<Post> m_posts;

	private NavDrawerAdapter.NavItem m_currentCategory;

	private boolean m_isActive;

	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		requestWindowFeature( Window.FEATURE_INDETERMINATE_PROGRESS );

		// Don't report starts during testing
		if( BuildConfig.DEBUG )
		{
			GoogleAnalytics.getInstance( this ).setDryRun( true );
		}

		setContentView( R.layout.activity_main_simple );

		PreferenceManager.setDefaultValues( this, R.xml.settings, false );

		m_drawerLayout = (DrawerLayout) findViewById( R.id.drawer_layout );

		m_drawerToggle =
				new DrawerToggle( this, m_drawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close );
		m_drawerLayout.setDrawerListener( m_drawerToggle );

		getActionBar().setDisplayHomeAsUpEnabled( true );
		getActionBar().setHomeButtonEnabled( true );

		m_navDrawerView = (ListView) findViewById( R.id.left_drawer );
		m_navDrawerAdapter = new NavDrawerAdapter( this );
		m_navDrawerView.setAdapter( m_navDrawerAdapter );
		m_navDrawerView.setOnItemClickListener( this );

		m_posts = new LinkedList<>();

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
		else
		{
			IntroFragment fragment = IntroFragment.newInstance();

			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction().replace( R.id.content_frame, fragment, CONTENT_FRAGMENT_TAG ).commit();
		}
	}

	protected void onSaveInstanceState( Bundle outState )
	{
		super.onSaveInstanceState( outState );

		outState.putSerializable( SAVE_POSTS, m_posts );
		outState.putSerializable( SAVE_NAV_ITEM, m_currentCategory );
	}

	@Override
	protected void onPostCreate( Bundle savedInstanceState )
	{
		super.onPostCreate( savedInstanceState );
		// Sync the toggle state after onRestoreInstanceState has occurred.
		m_drawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged( Configuration newConfig )
	{
		super.onConfigurationChanged( newConfig );
		m_drawerToggle.onConfigurationChanged( newConfig );
	}

	@Override
	public boolean onCreateOptionsMenu( Menu menu )
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate( R.menu.main, menu );
		return true;
	}

	@Override
	public boolean onOptionsItemSelected( MenuItem item )
	{
		final boolean handled;
		if( m_drawerToggle.onOptionsItemSelected( item ) )
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
		if( settings.getBoolean( "pref_key_keep_screen_on", true ) )
		{
			getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
		}
		else
		{
			getWindow().clearFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
		}
	}

	@Override
	public void onPause()
	{
		super.onPause();

		m_isActive = false;
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

	private void setNextImageButtonEnabled( boolean enabled )
	{
		if( m_isActive )
		{
			FragmentManager fragmentManager = getFragmentManager();
			Fragment fragment = fragmentManager.findFragmentByTag( CONTENT_FRAGMENT_TAG );
			if( fragment != null && fragment instanceof PostFragment )
			{
				PostFragment postFragment = (PostFragment) fragment;
				postFragment.setButtonEnabled( enabled );
			}
		}
	}

	public void onShowImageClicked( View view )
	{
		if( m_currentCategory == null )
		{
			m_currentCategory = NavDrawerAdapter.NavItem.all;
			setTitle();
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

				PostFragment fragment = PostFragment.newInstance( post, m_currentCategory );
				fragmentManager.beginTransaction().replace( R.id.content_frame, fragment, CONTENT_FRAGMENT_TAG ).commit();
			}
		}
		else
		{
			requestPosts();
		}
	}

	@Override
	public void onItemClick( AdapterView<?> parent, View view, int position, long id )
	{
		m_currentCategory = m_navDrawerAdapter.getItem( position );
		m_posts.clear();
		requestPosts();
		m_drawerLayout.closeDrawer( m_navDrawerView );

		Analytics.trackCategoryChange( this, m_currentCategory );
	}

	public void clearTitle()
	{
		String appName = getString( R.string.app_name );
		getActionBar().setTitle( appName );
	}

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

	private class RandditPostHandler implements Response.Listener<JSONObject>, Response.ErrorListener
	{
		public RandditPostHandler()
		{

		}

		@Override
		public void onResponse( JSONObject jsonObject )
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
		public void onErrorResponse( VolleyError volleyError )
		{
			Log.d( TAG, "Failed to retrieve posts." );
			Log.d( TAG, volleyError.toString() );

			setProgressBarIndeterminateVisibility( false );
			setNextImageButtonEnabled( true );
		}
	}

	private class DrawerToggle extends ActionBarDrawerToggle
	{
		public DrawerToggle( Activity activity, DrawerLayout drawerLayout, int drawerImageRes, int openDrawerContentDescRes, int closeDrawerContentDescRes )
		{
			super( activity, drawerLayout, drawerImageRes, openDrawerContentDescRes, closeDrawerContentDescRes );
		}

		public void onDrawerClosed( View view )
		{
			setTitle();
		}

		/**
		 * Called when a drawer has settled in a completely open state.
		 */
		public void onDrawerOpened( View drawerView )
		{
			clearTitle();
		}
	}
}
