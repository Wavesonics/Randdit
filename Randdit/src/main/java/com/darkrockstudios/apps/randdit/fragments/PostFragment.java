package com.darkrockstudios.apps.randdit.fragments;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import com.darkrockstudios.apps.randdit.DownloadService;
import com.darkrockstudios.apps.randdit.R;
import com.darkrockstudios.apps.randdit.RandditApplication;
import com.darkrockstudios.apps.randdit.misc.AdRequestBuilder;
import com.darkrockstudios.apps.randdit.misc.Analytics;
import com.darkrockstudios.apps.randdit.misc.NavDrawerAdapter;
import com.darkrockstudios.apps.randdit.misc.NextButtonEnabler;
import com.darkrockstudios.apps.randdit.misc.Post;
import com.darkrockstudios.apps.randdit.misc.Preferences;
import com.darkrockstudios.apps.randdit.misc.PurchaseScreenProvider;
import com.darkrockstudios.apps.randdit.misc.Tips;
import com.darkrockstudios.views.uriimageview.UriImageHandler;
import com.darkrockstudios.views.uriimageview.UriImageView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

/**
 * Created by Adam on 11/22/13.
 */
public class PostFragment extends Fragment implements View.OnClickListener, NextButtonEnabler, UriImageView.ImageZoomListener, View.OnSystemUiVisibilityChangeListener
{
	private static final String TAG = PostFragment.class.getSimpleName();

	private static final boolean IS_API_17_OR_LATER = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
	private static final boolean IS_API_18_OR_LATER = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
	private static final boolean IS_API_19_OR_LATER = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

	private static final String FRAGMENT_TAG_POST_INFO = "PostInfoFragment";
	private static final String FRAGMENT_TAG_TIP       = "TipFragment";

	private static final String ARG_PRO      = PostFragment.class.getName() + ".PRO";
	private static final String ARG_POST     = PostFragment.class.getName() + ".POST";
	private static final String ARG_CATEGORY = PostFragment.class.getName() + ".CATEGORY";

	private Tips.Tip m_tip;

	private boolean                  m_isPro;
	private Post                     m_post;
	private NavDrawerAdapter.NavItem m_category;
	private TextView                 m_titleView;
	private View                     m_toolBarView;
	private UriImageView             m_imageView;
	private Button                   m_nextPostButton;
	private UriImageHandler          m_imageHandler;
	private AdView                   m_adView;

	private PurchaseScreenProvider m_purchaseScreenProvider;

	private ShareActionProvider m_shareActionProvider;
	private AlertDialog         m_titleDialog;

	private boolean m_isTabletLayout;

	public static PostFragment newInstance( final Post post, final boolean isPro, final NavDrawerAdapter.NavItem category )
	{
		PostFragment fragment = new PostFragment();

		Bundle args = new Bundle();
		args.putBoolean( ARG_PRO, isPro );
		args.putSerializable( ARG_POST, post );
		args.putSerializable( ARG_CATEGORY, category );
		fragment.setArguments( args );

		return fragment;
	}

	@Override
	public void onAttach( final Activity activity )
	{
		super.onAttach( activity );

		if( activity instanceof PurchaseScreenProvider )
		{
			m_purchaseScreenProvider = (PurchaseScreenProvider) activity;
		}
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
			m_isPro = args.getBoolean( ARG_PRO );
			m_post = (Post) args.getSerializable( ARG_POST );
			m_category = (NavDrawerAdapter.NavItem) args.getSerializable( ARG_CATEGORY );
		}

		m_imageHandler = new UriImageHandler();

		Analytics.trackScreen( getActivity(), PostFragment.class.getSimpleName(), m_isPro );

		Activity activity = getActivity();
		if( activity != null )
		{
			View decorView = activity.getWindow().getDecorView();
			decorView.setOnSystemUiVisibilityChangeListener( this );
		}

		m_tip = Tips.shouldShowTip( getActivity(), m_isPro );
	}

	@Override
	public void onResume()
	{
		super.onResume();

		m_imageHandler.onResume( (UriImageView) getView().findViewById( R.id.POST_imageview ) );

		if( m_adView != null )
		{
			m_adView.resume();
		}

		if( m_tip != null )
		{
			DialogFragment tipDialog = Tips.constructTipDialog( m_tip );
			if( tipDialog != null )
			{
				tipDialog.show( getFragmentManager(), FRAGMENT_TAG_TIP );
				m_tip = null;
			}
		}
	}

	@Override
	public void onPause()
	{
		super.onPause();

		m_imageHandler.onPause();

		if( m_adView != null )
		{
			m_adView.pause();
		}
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();

		m_imageHandler.cancelDownload();

		if( m_adView != null )
		{
			m_adView.destroy();
		}
	}

	@Override
	public View onCreateView( final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState )
	{
		final View view;
		if( m_isPro )
		{
			view = inflater.inflate( R.layout.post_pro_fragment, container, false );
		}
		else
		{
			view = inflater.inflate( R.layout.post_fragment, container, false );
		}

		Uri uri = Uri.parse( m_post.url );

		m_imageView = (UriImageView) view.findViewById( R.id.POST_imageview );
		m_imageView.setErrorImage( R.drawable.image_error );
		m_imageHandler.loadImage( uri, m_imageView, RandditApplication.getImageCache() );
		m_imageView.setZoomListener( this );

		if( view.findViewById( R.id.POST_post_info_container ) != null )
		{
			m_isTabletLayout = true;
		}

		m_toolBarView = view.findViewById( R.id.POST_tool_bar );

		m_titleView = (TextView) view.findViewById( R.id.POST_title );
		if( m_titleView != null )
		{
			m_titleView.setText( Html.fromHtml( m_post.title ) );
			m_titleView.setOnClickListener( this );
		}

		m_nextPostButton = (Button) view.findViewById( R.id.POST_load_image_button );

		m_adView = (AdView) view.findViewById( R.id.POST_ad_view );
		if( m_adView != null )
		{
			AdRequest adRequest = AdRequestBuilder.buildRequest();
			m_adView.loadAd( adRequest );
		}

		return view;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	@Override
	public void onViewCreated( final View view, final Bundle savedInstanceState )
	{
		super.onViewCreated( view, savedInstanceState );

		ViewGroup postInfoContainer = (ViewGroup) view.findViewById( R.id.POST_post_info_container );

		if( postInfoContainer != null )
		{
			PostInfoFragment postInfoFragment = PostInfoFragment.newInstance( m_post );

			if( IS_API_17_OR_LATER )
			{
				getChildFragmentManager().beginTransaction().replace( R.id.POST_post_info_container, postInfoFragment )
						.commit();
			}
			else
			{
				getFragmentManager().beginTransaction().replace( R.id.POST_post_info_container, postInfoFragment ).commit();
			}
		}
	}

	@Override
	public void onCreateOptionsMenu( final Menu menu, final MenuInflater inflater )
	{
		super.onCreateOptionsMenu( menu, inflater );

		inflater.inflate( R.menu.post, menu );

		// Locate MenuItem with ShareActionProvider
		MenuItem item = menu.findItem( R.id.menu_item_share );

		// Fetch and store ShareActionProvider
		m_shareActionProvider = (ShareActionProvider) item.getActionProvider();

		Intent shareIntent = createShareIntent( m_post );
		if( shareIntent != null )
		{
			setShareIntent( shareIntent );
		}
	}

	@Override
	public boolean onOptionsItemSelected( final MenuItem item )
	{
		final boolean handled;

		Activity activity = getActivity();

		switch( item.getItemId() )
		{
			case R.id.menu_item_fullscreen:
			{
				if( activity != null && isAdded() )
				{
					Analytics.trackFullscreen( activity, m_category, m_isPro );

					if( IS_API_19_OR_LATER )
					{
						toggleImmersiveMode( activity );
					}
					else
					{
						toggleFullscreen( activity );
					}
				}
				handled = true;
			}
			break;
			case R.id.menu_item_download:
			{
				if( activity != null && isAdded() )
				{
					Uri uri = Uri.parse( m_post.url );
					Intent intent = new Intent( activity, DownloadService.class );
					intent.setData( uri );
					activity.startService( intent );

					Analytics.trackDownload( activity, m_category, m_isPro );
				}
				handled = true;
			}
			break;
			case R.id.menu_item_wallpaper:
			{
				if( activity != null && isAdded() )
				{
					if( m_isPro )
					{
						Uri uri = Uri.parse( m_post.url );
						Intent intent = new Intent( activity, DownloadService.class );
						intent.setData( uri );
						intent.putExtra( DownloadService.EXTRA_SET_WALLPAPER, true );
						activity.startService( intent );

						Analytics.trackWallpaper( activity, m_category, m_isPro );
					}
					else if( m_purchaseScreenProvider != null )
					{
						Analytics.trackProClick( getActivity(), "set_wallpaper" );

						m_purchaseScreenProvider.showPurchaseScreen();
					}
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


	private void toggleImmersiveMode( final Activity activity )
	{
		Window window = activity.getWindow();

		boolean visible = (window.getDecorView().getSystemUiVisibility() & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0;
		if( visible )
		{
			enterImmersiveMode( window );
		}
		else
		{
			exitImmersiveMode( window );
		}
	}

	@TargetApi(Build.VERSION_CODES.KITKAT)
	private void enterImmersiveMode( Window window )
	{
		View decorView = window.getDecorView();
		decorView.setSystemUiVisibility( View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
		                                 View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
		                                 View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
		                                 View.SYSTEM_UI_FLAG_FULLSCREEN |
		                                 View.SYSTEM_UI_FLAG_IMMERSIVE );
	}

	private void exitImmersiveMode( Window window )
	{
		View decorView = window.getDecorView();
		decorView.setSystemUiVisibility( 0 );
	}

	private void showSystemUI()
	{
		Activity activity = getActivity();
		if( activity != null )
		{
			View decorView = activity.getWindow().getDecorView();
			decorView.setSystemUiVisibility( View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
			                                 View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
			                                 View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN );
		}
	}

	private void toggleFullscreen( final Activity activity )
	{
		Window window = activity.getWindow();
		WindowManager.LayoutParams attrs = window.getAttributes();
		if( (attrs.flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) == 0 )
		{
			attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
		}
		else
		{
			attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
		}
		window.setAttributes( attrs );
	}

	public static String createRandditUrl( final Post post, final NavDrawerAdapter.NavItem category )
	{
		String randditBase = "http://randdit.com/";
		String categoryStr = NavDrawerAdapter.getId( category );

		String url = randditBase + categoryStr + '/' + post.id;

		return url;
	}

	private Intent createShareIntent( final Post post )
	{
		final Intent intent;

		if( post != null )
		{
			intent = new Intent( Intent.ACTION_SEND );
			intent.setData( Uri.parse( post.url ) );

			Activity activity = getActivity();
			boolean appendAd = false;
			boolean shareText = false;
			if( activity != null )
			{
				SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( activity );
				appendAd = settings.getBoolean( Preferences.KEY_APPEND_AD, true );
				shareText = settings.getBoolean( Preferences.KEY_SHARE_TEXT, false );
			}

			final String shareBody;
			if( shareText )
			{
				final int shareBodyResource;
				if( appendAd )
				{
					shareBodyResource = R.string.share_body;
				}
				else
				{
					shareBodyResource = R.string.share_body_no_ad;
				}

				shareBody = getString( shareBodyResource, Html.fromHtml( post.title ), createRandditUrl( post, m_category ) );
			}
			else
			{
				shareBody = createRandditUrl( post, m_category );
			}

			intent.putExtra( Intent.EXTRA_TEXT, shareBody );
			intent.setType( "text/plain" );
		}
		else
		{
			intent = null;
		}

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

		m_purchaseScreenProvider = null;

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
		FragmentManager fragmentManager = getFragmentManager();
		if( fragmentManager != null )
		{
			PostInfoFragment postInfoFragment = PostInfoFragment.newInstance( m_post );
			postInfoFragment.show( fragmentManager, FRAGMENT_TAG_POST_INFO );
		}
	}

	public Post getPost()
	{
		return m_post;
	}

	private boolean shouldDismissUi()
	{
		return !m_isTabletLayout && IS_API_18_OR_LATER;
	}

	@Override
	public void beganZooming( final UriImageView uriImageView )
	{
		final long DURATION = 400;

		if( shouldDismissUi() )
		{
			ObjectAnimator toolbarAnim = ObjectAnimator.ofFloat( m_toolBarView, "translationY", m_toolBarView.getHeight() );
			toolbarAnim.setInterpolator( new AccelerateDecelerateInterpolator() );
			toolbarAnim.setDuration( DURATION );
			toolbarAnim.start();

			if( m_titleView != null )
			{
				ObjectAnimator titleAnim = ObjectAnimator.ofFloat( m_titleView, "translationY", -m_toolBarView.getHeight() );
				titleAnim.setInterpolator( new AccelerateDecelerateInterpolator() );
				titleAnim.setDuration( DURATION );
				titleAnim.start();
			}
		}
	}

	@Override
	public void endedZooming( final UriImageView uriImageView )
	{
		if( shouldDismissUi() )
		{
			ObjectAnimator toolbarAnim = ObjectAnimator.ofFloat( m_toolBarView, "translationY", 0.0f );
			toolbarAnim.setInterpolator( new AccelerateDecelerateInterpolator() );
			toolbarAnim.start();

			if( m_titleView != null )
			{
				ObjectAnimator titleAnim = ObjectAnimator.ofFloat( m_titleView, "translationY", 0.0f );
				titleAnim.setInterpolator( new AccelerateDecelerateInterpolator() );
				titleAnim.start();
			}
		}
	}

	@Override
	public void onSystemUiVisibilityChange( final int flags )
	{
		Activity activity = getActivity();

		if( activity != null && IS_API_19_OR_LATER )
		{
			boolean visible = (flags & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0;
			if( visible )
			{
				exitImmersiveMode( activity.getWindow() );
			}
		}
	}
}
