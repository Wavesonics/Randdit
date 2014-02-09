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

import com.darkrockstudios.apps.randdit.R;
import com.darkrockstudios.apps.randdit.googleplaygames.GameHelper;
import com.google.android.gms.common.SignInButton;

import java.io.Serializable;

/**
 * Created by Adam on 11/22/13.
 */
public class NavDrawerAdapter extends ArrayAdapter<NavDrawerAdapter.NavItem> implements View.OnClickListener
{
	public static NavItem CATEGORY_ALL = new NavItem( new CategoryDefinition( "all", "", 1 ) );

	public static enum OtherNavItemType
	{
		GoogleSignIn,
		Leaderboards,
		ProAd
	}

	public static class NavItem implements Serializable
	{
		public final CategoryDefinition category;
		public final boolean          isCategory;
		public final OtherNavItemType type;

		public NavItem( final OtherNavItemType type )
		{
			category = null;
			isCategory = false;
			this.type = type;
		}

		public NavItem( final CategoryDefinition category )
		{
			this.category = category;
			isCategory = true;
			type = null;
		}

		public String toString()
		{
			final String string;
			if( !isCategory )
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
				string = type.toString();
			}

			return string;
		}
	}

	private GameHelper m_helper;
	private Categories m_categories;

	private static final int TYPE_CATEGORY    = 0;
	private static final int TYPE_SIGN_IN     = 1;
	private static final int TYPE_LEADERBOARD = 2;
	private static final int TYPE_PRO_AD      = 3;
	private static final int TYPE_COUNT       = 4;

	private boolean m_isPro;
	private boolean m_signedIn;

	public NavDrawerAdapter( final Context context )
	{
		super( context, android.R.layout.simple_list_item_1 );

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( context );
		m_isPro = settings.getBoolean( Preferences.KEY_IS_PRO, false );

		refreshNavItems();
	}

	public void setSignedIn( final boolean signedIn )
	{
		m_signedIn = signedIn;
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

	public void setGameHelper( final GameHelper helper )
	{
		m_helper = helper;
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

		if( !m_signedIn )
		{
			add( new NavItem( OtherNavItemType.GoogleSignIn ) );
		}
		else
		{
			add( new NavItem( OtherNavItemType.Leaderboards ) );
		}

		if( !m_isPro )
		{
			add( new NavItem( OtherNavItemType.ProAd ) );
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

		if( !navItem.isCategory )
		{
			switch( navItem.type )
			{
				case GoogleSignIn:
					type = TYPE_SIGN_IN;
					break;
				case Leaderboards:
					type = TYPE_LEADERBOARD;
					break;
				case ProAd:
					type = TYPE_PRO_AD;
					break;
				default:
					throw new Error( "Bad nav item: " + navItem.type );
			}
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
				case TYPE_SIGN_IN:
					view = inflater.inflate( R.layout.nav_drawer_item_signin, parent, false );
					break;
				case TYPE_LEADERBOARD:
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
			case TYPE_SIGN_IN:
				SignInButton signInButton = (SignInButton) view.findViewById( R.id.sign_in_button );
				signInButton.setOnClickListener( this );
				break;
			case TYPE_LEADERBOARD:
			{
				TextView titleView = (TextView) view.findViewById( android.R.id.text1 );
				titleView.setTextColor( Color.WHITE );
				titleView.setText( R.string.nav_pro_leaderboards );
			}
			break;
			case TYPE_PRO_AD:
			{
				TextView titleView = (TextView) view.findViewById( android.R.id.text1 );
				titleView.setTextColor( Color.WHITE );
				titleView.setText( R.string.nav_pro_title );

				TextView subtitleView = (TextView) view.findViewById( android.R.id.text2 );
				subtitleView.setTextColor( Color.WHITE );
				subtitleView.setText( R.string.nav_pro_summary );
			}
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

	@Override
	public void onClick( final View v )
	{
		if( m_helper != null )
		{
			m_helper.beginUserInitiatedSignIn();
		}
	}
}
