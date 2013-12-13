package com.darkrockstudios.apps.randdit.fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.darkrockstudios.apps.randdit.R;
import com.darkrockstudios.apps.randdit.misc.Post;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by Adam on 12/8/13.
 */
public class PostInfoFragment extends DialogFragment
{
	private static final String ARG_POST        = "POST";
	private static final String REDDIT_BASE_URL = "http://reddit.com";

	private Post m_post;

	public static PostInfoFragment newInstance( final Post post )
	{
		PostInfoFragment fragment = new PostInfoFragment();

		Bundle args = new Bundle();
		args.putSerializable( ARG_POST, post );
		fragment.setArguments( args );

		return fragment;
	}

	@Override
	public void onCreate( final Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		Bundle args = getArguments();
		if( args != null )
		{
			m_post = (Post) args.getSerializable( ARG_POST );
		}
	}

	@Override
	public View onCreateView( final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState )
	{
		View view;

		view = inflater.inflate( R.layout.post_info_fragment, container, false );

		if( view != null && m_post != null )
		{
			TextView titleView = (TextView) view.findViewById( R.id.POSTINFO_title );
			titleView.setText( Html.fromHtml( m_post.title ) );

			TextView userView = (TextView) view.findViewById( R.id.POSTINFO_user );
			userView.setText( m_post.author );

			TextView createdDateView = (TextView) view.findViewById( R.id.POSTINFO_created );
			createdDateView.setText( parseDate( m_post.created ) );

			TextView permalinkView = (TextView) view.findViewById( R.id.POSTINFO_permalink );
			permalinkView.setText( REDDIT_BASE_URL + m_post.permalink );
			permalinkView.setSelected( true );
		}

		Dialog dialog = getDialog();
		if( dialog != null )
		{
			dialog.setTitle( R.string.postinfo_title );
		}

		return view;
	}

	private String parseDate( final String timeStampStr )
	{
		final String dateStr;

		if( timeStampStr != null )
		{
			long timeStampSeconds = Long.parseLong( timeStampStr );
			Date createdDate = new Date( timeStampSeconds * 1000 );
			DateFormat dateFormat = DateFormat.getDateInstance();
			dateStr = dateFormat.format( createdDate );
		}
		else
		{
			dateStr = getString( R.string.postinfo_no_creation_date );
		}

		return dateStr;
	}
}
