package com.darkrockstudios.apps.randdit;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.darkrockstudios.apps.randdit.misc.NavDrawerAdapter;

/**
 * Created by Adam on 12/8/13.
 */
public abstract class NavDrawerActivity extends BillingActivity implements AdapterView.OnItemClickListener
{
	protected DrawerToggle     m_drawerToggle;
	protected DrawerLayout     m_drawerLayout;
	protected ListView         m_navDrawerView;
	protected NavDrawerAdapter m_navDrawerAdapter;

	private class DrawerToggle extends ActionBarDrawerToggle
	{
		public DrawerToggle( final Activity activity, final DrawerLayout drawerLayout, final int drawerImageRes, final int openDrawerContentDescRes, final int closeDrawerContentDescRes )
		{
			super( activity, drawerLayout, drawerImageRes, openDrawerContentDescRes, closeDrawerContentDescRes );
		}

		public void onDrawerClosed( final View view )
		{
			setTitle();
		}

		/**
		 * Called when a drawer has settled in a completely open state.
		 */
		public void onDrawerOpened( final View drawerView )
		{
			clearTitle();
		}
	}

	@Override
	protected void onCreate( final Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_nav_drawer );

		setupNavDrawer();
	}

	@Override
	protected void onPostCreate( final Bundle savedInstanceState )
	{
		super.onPostCreate( savedInstanceState );
		// Sync the toggle state after onRestoreInstanceState has occurred.
		m_drawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged( final Configuration newConfig )
	{
		super.onConfigurationChanged( newConfig );
		m_drawerToggle.onConfigurationChanged( newConfig );
	}

	@Override
	public boolean onOptionsItemSelected( final MenuItem item )
	{
		final boolean handled;
		if( m_drawerToggle.onOptionsItemSelected( item ) )
		{
			handled = true;
		}
		else
		{
			handled = super.onOptionsItemSelected( item );
		}

		return handled;
	}

	private void setupNavDrawer()
	{
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
	}

	public void clearTitle()
	{
		String appName = getString( R.string.app_name );
		getActionBar().setTitle( appName );
	}

	public abstract void setTitle();

	public abstract void onItemClick( final AdapterView<?> parent, final View view, final int position, final long id );
}
