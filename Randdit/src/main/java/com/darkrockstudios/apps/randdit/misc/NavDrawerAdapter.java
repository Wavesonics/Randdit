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

import java.io.Serializable;

/**
 * Created by Adam on 11/22/13.
 */
public class NavDrawerAdapter extends ArrayAdapter<NavDrawerAdapter.NavItem>
{
	public static NavItem CATEGORY_ALL = new NavItem( new CategoryDefinition( "all", "", 1 ) );

	public static class NavItem implements Serializable
	{
		public final CategoryDefinition category;
		public final boolean            pro;

		public NavItem()
		{
			category = null;
			pro = true;
		}

		public NavItem( final CategoryDefinition category )
		{
			this.category = category;
			pro = false;
		}

		public String toString()
		{
			final String string;
			if( !pro )
			{
				if( category != null && category.name != null )
				{
					string = category.name;
				}
				else
				{
					string = "Bad Category";
				}
			}
			else
			{
				string = "Pro Ad";
			}

			return string;
		}
	}

	private Categories m_categories;

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

	public void setCategories( final Categories categories )
	{
		m_categories = categories;
		refreshNavItems();
	}

	public void refreshNavItems()
	{
		clear();

		if( m_categories != null )
		{
			final boolean wtfEnabled = wtfEnabled();
			for( final CategoryDefinition definition : m_categories.categories )
			{
				if( definition.is_sfw == 1 )
				{
					add( new NavItem( definition ) );
				}
				else if( definition.is_sfw == 0 && wtfEnabled )
				{
					add( new NavItem( definition ) );
				}
			}
		}

		if( !m_isPro )
		{
			add( new NavItem() );
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

		NavItem navItem = getItem( position );

		if( !m_isPro && navItem.pro )
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

		if( item != null && item.category != null )
		{
			title = item.category.name;
		}
		else
		{
			title = null;
		}

		return title;
	}
}
