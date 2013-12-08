package com.darkrockstudios.apps.randdit.fragments;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import com.darkrockstudios.apps.randdit.DownloadService;
import com.darkrockstudios.apps.randdit.R;
import com.darkrockstudios.apps.randdit.RandditApplication;
import com.darkrockstudios.apps.randdit.misc.Analytics;
import com.darkrockstudios.apps.randdit.misc.NavDrawerAdapter;
import com.darkrockstudios.apps.randdit.misc.NextButtonEnabler;
import com.darkrockstudios.apps.randdit.misc.Post;
import com.darkrockstudios.views.uriimageview.UriImageHandler;
import com.darkrockstudios.views.uriimageview.UriImageView;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;

/**
 * Created by Adam on 11/22/13.
 */
public class PostFragment extends Fragment implements View.OnClickListener, NextButtonEnabler, UriImageView.ImageZoomListener
{
	private static final String TAG = PostFragment.class.getSimpleName();

	private static final String ARG_POST = PostFragment.class.getName() + ".POST";
	private static final String ARG_CATEGORY = PostFragment.class.getName() + ".CATEGORY";

	private Post            m_post;
	private NavDrawerAdapter.NavItem m_category;
	private TextView        m_titleView;
	private View            m_toolBarView;
	private UriImageView    m_imageView;
	private Button          m_nextPostButton;
	private UriImageHandler m_imageHandler;

	private ShareActionProvider m_shareActionProvider;
	private AlertDialog         m_titleDialog;

	public static PostFragment newInstance( final Post post, final NavDrawerAdapter.NavItem category )
	{
		PostFragment fragment = new PostFragment();

		Bundle args = new Bundle();
		args.putSerializable( ARG_POST, post );
		args.putSerializable( ARG_CATEGORY, category );
		fragment.setArguments( args );

		return fragment;
	}

	@Override
	public void onCreate( final Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		setHasOptionsMenu( true );
		setRetainInstance( true );

		Bundle args = getArguments();
		if( args != null )
		{
			m_post = (Post) args.getSerializable( ARG_POST );
			m_category = (NavDrawerAdapter.NavItem) args.getSerializable( ARG_CATEGORY );
		}

		m_imageHandler = new UriImageHandler();

		EasyTracker tracker = EasyTracker.getInstance( getActivity() );
		tracker.set( Fields.SCREEN_NAME, PostFragment.class.getSimpleName() );
		tracker.send( MapBuilder.createAppView().build() );
	}

	@Override
	public void onResume()
	{
		super.onResume();

		m_imageHandler.onResume( (UriImageView) getView().findViewById( R.id.POST_imageview ) );
	}

	@Override
	public void onPause()
	{
		super.onPause();

		m_imageHandler.onPause();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();

		m_imageHandler.cancelDownload();
	}

	@Override
	public View onCreateView( final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState )
	{
		View view = inflater.inflate( R.layout.post_fragment, container, false );

		Uri uri = Uri.parse( m_post.url );

		m_imageView = (UriImageView) view.findViewById( R.id.POST_imageview );
		m_imageView.setErrorImage( R.drawable.image_error );
		m_imageHandler.loadImage( uri, m_imageView, RandditApplication.getImageCache() );
		m_imageView.setZoomListener( this );

		m_toolBarView = view.findViewById( R.id.POST_tool_bar );

		m_titleView = (TextView) view.findViewById( R.id.POST_title );
		m_titleView.setText( Html.fromHtml( m_post.title ) );
		m_titleView.setOnClickListener( this );

		m_nextPostButton = (Button) view.findViewById( R.id.POST_load_image_button );

		return view;
	}

	@Override
	public void onCreateOptionsMenu( final Menu menu, final MenuInflater inflater )
	{
		super.onCreateOptionsMenu( menu, inflater );

		inflater.inflate( R.menu.post_menu, menu );

		// Locate MenuItem with ShareActionProvider
		MenuItem item = menu.findItem( R.id.menu_item_share );

		// Fetch and store ShareActionProvider
		m_shareActionProvider = (ShareActionProvider) item.getActionProvider();

		setShareIntent( createShareIntent( m_post ) );
	}

	@Override
	public boolean onOptionsItemSelected( final MenuItem item )
	{
		final boolean handled;

		Activity activity = getActivity();

		switch( item.getItemId() )
		{
			case R.id.menu_item_download:
			{
				if( activity != null && isAdded() )
				{
					Uri uri = Uri.parse( m_post.url );
					Intent intent = new Intent( activity, DownloadService.class );
					intent.setData( uri );
					activity.startService( intent );

					Analytics.trackDownload( activity, m_category );
				}
				handled = true;
			}
			break;
			case R.id.menu_item_wallpaper:
			{
				if( activity != null && isAdded() )
				{
					Uri uri = Uri.parse( m_post.url );
					Intent intent = new Intent( activity, DownloadService.class );
					intent.setData( uri );
					intent.putExtra( DownloadService.EXTRA_SET_WALLPAPER, true );
					activity.startService( intent );

					Analytics.trackWallpaper( activity, m_category );
				}
				handled = true;
			}
			break;
			default:
				handled = super.onOptionsItemSelected( item );
				break;
		}

		return handled;
	}

	private String createRandditUrl( final Post post )
	{
		String randditBase = "http://randdit.com/";
		String category = NavDrawerAdapter.getId( m_category );

		String url = randditBase + category + '/' + post.id;

		return url;
	}

	private Intent createShareIntent( final Post post )
	{
		Intent intent = new Intent( Intent.ACTION_SEND );
		intent.setData( Uri.parse( post.url ) );
		String shareBody = getString( R.string.share_body, Html.fromHtml( post.title ), createRandditUrl( post ) );
		intent.putExtra( Intent.EXTRA_TEXT, shareBody );
		intent.setType( "image/*" );

		return intent;
	}

	private void setShareIntent( final Intent shareIntent )
	{
		if( m_shareActionProvider != null )
		{
			m_shareActionProvider.setShareIntent( shareIntent );
		}
	}

	public void onDetach()
	{
		super.onDetach();

		if( m_titleDialog != null )
		{
			m_titleDialog.dismiss();
			m_titleDialog = null;
		}
	}

	@Override
	public void setNextButtonEnabled( final boolean enabled )
	{
		if( isAdded() && m_nextPostButton != null )
		{
			m_nextPostButton.setEnabled( enabled );
		}
	}

	@Override
	public void onClick( final View v )
	{
		final Context context = v.getContext();
		if( context != null )
		{
			AlertDialog.Builder builder = new AlertDialog.Builder( context );
			builder.setMessage( Html.fromHtml( m_post.title ) );
			m_titleDialog = builder.create();
			m_titleDialog.show();
		}
	}

	@Override
	public void beganZooming( final UriImageView uriImageView )
	{
		ObjectAnimator toolbarAnim = ObjectAnimator.ofFloat( m_toolBarView, "translationY", m_toolBarView.getHeight() );
		toolbarAnim.setInterpolator( new AccelerateDecelerateInterpolator() );
		toolbarAnim.setDuration( 400 );
		toolbarAnim.start();

		ObjectAnimator titleAnim = ObjectAnimator.ofFloat( m_titleView, "translationY", -m_toolBarView.getHeight() );
		titleAnim.setInterpolator( new AccelerateDecelerateInterpolator() );
		titleAnim.setDuration( 400 );
		titleAnim.start();
	}

	@Override
	public void endedZooming( final UriImageView uriImageView )
	{
		ObjectAnimator toolbarAnim = ObjectAnimator.ofFloat( m_toolBarView, "translationY", 0.0f );
		toolbarAnim.setInterpolator( new AccelerateDecelerateInterpolator() );
		toolbarAnim.start();

		ObjectAnimator titleAnim = ObjectAnimator.ofFloat( m_titleView, "translationY", 0.0f );
		titleAnim.setInterpolator( new AccelerateDecelerateInterpolator() );
		titleAnim.start();
	}
}
