package com.darkrockstudios.apps.randdit;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.vending.billing.IInAppBillingService;
import com.darkrockstudios.apps.randdit.billing.Security;
import com.darkrockstudios.apps.randdit.misc.Preferences;
import com.darkrockstudios.apps.randdit.misc.PurchaseProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Adam on 12/10/13.
 */
public class BillingActivity extends Activity implements PurchaseProvider
{
	private static final String TAG             = BillingActivity.class.getSimpleName();
	private static final String PRODUCT_SKU_PRO = "randdit_pro";
	//private static final String PRODUCT_SKU_PRO = "android.test.purchased";

	private boolean m_isPro;

	private IInAppBillingService     m_service;
	private BillingServiceConnection m_serviceConn;

	private ProStatusListener m_statusListener;

	public static interface ProStatusListener
	{
		public void onProStatusUpdate( final boolean isPro );
	}

	private class BillingServiceConnection implements ServiceConnection
	{
		@Override
		public void onServiceDisconnected( final ComponentName name )
		{
			m_service = null;
		}

		@Override
		public void onServiceConnected( final ComponentName name,
		                                final IBinder service )
		{
			m_service = IInAppBillingService.Stub.asInterface( service );

			Log.d( TAG, "Billing service connected." );

			if( !m_isPro )
			{
				runProCheck();
			}
		}
	}

	public void setProStatusListener( final ProStatusListener listener )
	{
		m_statusListener = listener;
	}

	@Override
	protected void onCreate( final Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( this );
		m_isPro = settings.getBoolean( Preferences.KEY_IS_PRO, false );

		m_serviceConn = new BillingServiceConnection();
		connectToService();
	}

	private void connectToService()
	{
		bindService( new Intent( "com.android.vending.billing.InAppBillingService.BIND" ),
		             m_serviceConn, Context.BIND_AUTO_CREATE );
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if( m_serviceConn != null )
		{
			unbindService( m_serviceConn );
		}
	}

	public boolean isPro()
	{
		return m_isPro;
	}

	public void runProCheck()
	{
		ProCheckThread proCheckThread = new ProCheckThread();
		proCheckThread.start();
	}

	private class ProCheckThread extends Thread
	{
		@Override
		public void run()
		{
			checkForPro();

			if( m_statusListener != null )
			{
				m_statusListener.onProStatusUpdate( isPro() );
			}
		}
	}

	private void checkForPro()
	{
		Log.d( TAG, "Checking pro purchase status..." );

		if( m_service != null )
		{
			try
			{
				Log.d( TAG, "sending request..." );
				Bundle ownedItems = m_service.getPurchases( 3, getPackageName(), "inapp", null );
				Log.d( TAG, "Request received!" );
				int response = ownedItems.getInt( "RESPONSE_CODE" );
				if( response == 0 )
				{
					ArrayList<String> ownedSkus = ownedItems.getStringArrayList( "INAPP_PURCHASE_ITEM_LIST" );
					ArrayList<String> purchaseDataList = ownedItems.getStringArrayList( "INAPP_PURCHASE_DATA_LIST" );
					//ArrayList<String> signatureList = ownedItems.getStringArrayList( "INAPP_DATA_SIGNATURE" );
					//String continuationToken = ownedItems.getString( "INAPP_CONTINUATION_TOKEN" );

					for( int i = 0; i < purchaseDataList.size(); ++i )
					{
						String purchaseData = purchaseDataList.get( i );
						//String signature = signatureList.get( i );
						String sku = ownedSkus.get( i );

						if( sku.equals( PRODUCT_SKU_PRO ) )
						{
							Log.d( TAG, "Holy crap we're pro!" );
							cacheProLocally();
							m_isPro = true;
						}
					}
				}

				if( !m_isPro )
				{
					Log.d( TAG, "I don't think we're pro :(" );
				}
			}
			catch( RemoteException e )
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public void purchasePro()
	{
		if( m_service != null )
		{
			try
			{
				Bundle buyIntentBundle =
						m_service.getBuyIntent( 3, getPackageName(), PRODUCT_SKU_PRO, "inapp", "empty_payload" );

				int responseCode = buyIntentBundle.getInt( "RESPONSE_CODE", 0 );
				if( responseCode == 0 )
				{
					PendingIntent pendingIntent = buyIntentBundle.getParcelable( "BUY_INTENT" );

					startIntentSenderForResult( pendingIntent.getIntentSender(),
					                            1001, new Intent(),
					                            Integer.valueOf( 0 ),
					                            Integer.valueOf( 0 ),
					                            Integer.valueOf( 0 ) );
				}
			}
			catch( RemoteException | IntentSender.SendIntentException e )
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onActivityResult( final int requestCode, final int resultCode, final Intent data )
	{
		if( requestCode == 1001 )
		{
			int responseCode = data.getIntExtra( "RESPONSE_CODE", 0 );
			String purchaseData = data.getStringExtra( "INAPP_PURCHASE_DATA" );
			String dataSignature = data.getStringExtra( "INAPP_DATA_SIGNATURE" );

			if( resultCode == RESULT_OK )
			{
				try
				{
					JSONObject jo = new JSONObject( purchaseData );
					String sku = jo.getString( "productId" );
					// verifyPurchase( purchaseData, dataSignature )
					if( PRODUCT_SKU_PRO.equals( sku ) )
					{
						Log.d( TAG, "You have bought " + sku );

						cacheProLocally();
						m_statusListener.onProStatusUpdate( true );
					}
					else
					{
						Log.e( TAG, "Purchase failed." );
					}
				}
				catch( JSONException e )
				{
					Log.d( TAG, "Failed to parse purchase data." );
					e.printStackTrace();
				}
			}
		}
	}

	private void cacheProLocally()
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( this );
		settings.edit().putBoolean( Preferences.KEY_IS_PRO, true ).commit();
	}

	private String assemblePublicKey()
	{
		return "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAm81R0ci/UDjZfJ+2ySzwn7mbBhfnb0H5aQhcgOe90FKpNNNy7/FBmrkh1v3N7Iha/siI77tyJsevzpssqvUHkA24dJZ6tEe+lGc2b6aGRpkDNjQeLZsb93525LBDH7rFhhoI+tcqjfZ2D8dIvS273wfWA2I4YiEvfwwrrsZvb4AKHHmavW+zqZRSs7pD+Mm1X1VvSMmFyX+6e/O974ptkKzd111VeozV3pbIJZ3Rl6YeEHHS32YVlc4Ae9vP1W1P96ICpfOufyBO5e77O6sf7drTFHp45I9E0QGhEIwIT3VqeMiCmQU3iYSOMp3bgCLTOsDVeVuy7WHac46XzCFkPwIDAQAB";
	}

	private boolean verifyPurchase( final String data, final String signature )
	{
		return Security.verifyPurchase( assemblePublicKey(), data, signature );
	}
}
