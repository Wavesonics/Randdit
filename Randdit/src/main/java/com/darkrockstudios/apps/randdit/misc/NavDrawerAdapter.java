package com.darkrockstudios.apps.randdit.misc;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by Adam on 11/22/13.
 */
public class NavDrawerAdapter extends ArrayAdapter<NavDrawerAdapter.NavItem>
{
	public static enum NavItem
	{
		all,
		new_,
		funny,
		cute,
		beautiful,
		gifs,
		wtf,
		pro
	}

	private static final int TYPE_CATEGORY = 0;
	private static final int TYPE_PRO_AD   = 1;
	private static final int TYPE_COUNT    = 2;

	private boolean m_isPro;

	public NavDrawerAdapter( final Context context )
	{
		super( context, android.R.layout.simple_list_item_1 );

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( context );
		m_isPro = settings.getBoolean( Preferences.KEY_IS_PRO, false );
		;

		refreshNavItems();
	}

	public void setPro( final boolean isPro )
	{
		m_isPro = isPro;
	}

	public void refreshNavItems()
	{
		clear();

		add( NavItem.all );
		add( NavItem.new_ );
		add( NavItem.funny );
		add( NavItem.cute );
		add( NavItem.beautiful );
		add( NavItem.gifs );
		if( wtfEnabled() )
		{
			add( NavItem.wtf );
		}

		if( !m_isPro )
		{
			add( NavItem.pro );
		}
	}

	private boolean wtfEnabled()
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( getContext() );
		return settings.getBoolean( Preferences.KEY_SHOW_WTF, false );
	}

	@Override
	public int getViewTypeCount()
	{
		return TYPE_COUNT;
	}

	@Override
	public int getItemViewType( final int position )
	{
		final int type;

		if( !m_isPro && position == getCount() - 1 )
		{
			type = TYPE_PRO_AD;
		}
		else
		{
			type = TYPE_CATEGORY;
		}

		return type;
	}

	@Override
	public View getView( final int position, final View convertView, final ViewGroup parent )
	{
		final int type = getItemViewType( position );

		final View view;
		if( convertView == null )
		{
			LayoutInflater inflater = LayoutInflater.from( getContext() );
			switch( type )
			{
				case TYPE_CATEGORY:
					view = inflater.inflate( android.R.layout.simple_list_item_1, parent, false );
					break;
				case TYPE_PRO_AD:
				default:
					view = inflater.inflate( android.R.layout.simple_list_item_2, parent, false );
					break;
			}

		}
		else
		{
			view = convertView;
		}

		switch( type )
		{
			case TYPE_CATEGORY:
				String title = getId( getItem( position ) );
				if( title != null )
				{
					TextView titleView = (TextView) view.findViewById( android.R.id.text1 );
					titleView.setTextColor( Color.WHITE );
					titleView.setText( title );
				}
				break;
			case TYPE_PRO_AD:
				TextView titleView = (TextView) view.findViewById( android.R.id.text1 );
				titleView.setTextColor( Color.WHITE );
				titleView.setText( "Go Pro" );

				TextView subtitleView = (TextView) view.findViewById( android.R.id.text2 );
				subtitleView.setTextColor( Color.WHITE );
				subtitleView.setText( "Remove Ads!" );
				break;
		}

		return view;
	}

	public static String getId( final NavItem item )
	{
		final String title;

		if( item != null )
		{
			switch( item )
			{
				case all:
					title = "all";
					break;
				case new_:
					title = "new";
					break;
				case funny:
					title = "funny";
					break;
				case cute:
					title = "cute";
					break;
				case beautiful:
					title = "beautiful";
					break;
				case gifs:
					title = "gifs";
					break;
				case wtf:
					title = "wtf";
					break;
				default:
					title = null;
					break;
			}
		}
		else
		{
			title = null;
		}

		return title;
	}
}
