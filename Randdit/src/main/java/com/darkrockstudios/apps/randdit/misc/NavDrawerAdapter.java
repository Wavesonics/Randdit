package com.darkrockstudios.apps.randdit.misc;

import android.content.Context;
import android.graphics.Color;
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
		wtf
	}

	public NavDrawerAdapter( Context context )
	{
		super( context, android.R.layout.simple_list_item_1 );

		add( NavItem.all );
		add( NavItem.new_ );
		add( NavItem.funny );
		add( NavItem.cute );
		add( NavItem.beautiful );
		add( NavItem.gifs );
		add( NavItem.wtf );
	}

	@Override
	public View getView (int position, View convertView, ViewGroup parent)
	{
		final View view;
		if( convertView == null )
		{
			LayoutInflater inflater = LayoutInflater.from( getContext() );
			view = inflater.inflate( android.R.layout.simple_list_item_1, parent, false );
		}
		else
		{
			view = convertView;
		}

		String title = getId( getItem( position ) );
		if( title != null )
		{
			TextView titleView = (TextView) view.findViewById( android.R.id.text1 );
			titleView.setTextColor( Color.WHITE );
			titleView.setText( title );
		}

		return view;
	}

	public static String getId( NavItem item )
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
