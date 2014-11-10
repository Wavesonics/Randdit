package com.darkrockstudios.apps.randdit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.darkrockstudios.apps.randdit.billing.BillingSecurity;
import com.darkrockstudios.apps.randdit.googleplaygames.BaseGameActivity;
import com.darkrockstudios.apps.randdit.misc.Preferences;
import com.darkrockstudios.apps.randdit.misc.PurchaseProvider;
import com.darkrockstudios.util.IabException;
import com.darkrockstudios.util.IabHelper;
import com.darkrockstudios.util.IabResult;
import com.darkrockstudios.util.Inventory;
import com.darkrockstudios.util.Purchase;
import com.darkrockstudios.util.Security;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by Adam on 12/10/13.
 */
public abstract class BillingActivity extends BaseGameActivity implements PurchaseProvider, IabHelper.OnIabPurchaseFinishedListener
{
	private static final String TAG             = BillingActivity.class.getSimpleName();
	private static final String PRODUCT_SKU_PRO = "randdit_pro";
	//private static final String PRODUCT_SKU_PRO = "android.test.purchased";

	private static final String EENY  =
			"ZVVOVwNAOtxduxvT9j0ONDRSNNBPND8NZVVOPtXPNDRNz81E0pv/HQwMsW+2lFmja7zoOusao0U5nDuptBr90SXcAAAl7/SOze";
	private static final String MEENY =
			"xu1i3A7Vun/fvV77glWfrimcffdiHUxN24qWM6gRr+yTp2o6nTEcxQAwDrYMfo93525YOQU7eSuubV+gpdwsM2Q8qViF273jsJ";

	private IabHelper m_billingHelper;

	private boolean m_isPro;

	private ProStatusListener m_statusListener;

	private String m_devPayload;

	@Override
	public void onIabPurchaseFinished( IabResult result, Purchase info )
	{

	}

	public static interface ProStatusListener
	{
		public void onProStatusUpdate( final boolean isPro );
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

		m_billingHelper = new IabHelper( this, assemblePublicKey() );

		connectToService();
	}

	private void connectToService()
	{
		m_billingHelper.startSetup( new IabHelper.OnIabSetupFinishedListener()
		{
			public void onIabSetupFinished( IabResult result )
			{
				if( !result.isSuccess() )
				{
					// Oh noes, there was a problem.
					Log.d( TAG, "Problem setting up In-app Billing: " + result );
				}
				else
				{
					Log.d( TAG, "Billing service connected." );

					if( !m_isPro )
					{
						runProCheck();
					}
				}
			}
		} );
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if ( m_billingHelper != null) m_billingHelper.dispose();
		m_billingHelper = null;
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
		try
		{
			Inventory inventory = m_billingHelper.queryInventory( false, null, null );

			if( inventory.hasPurchase( PRODUCT_SKU_PRO ) )
			{
				Log.d( TAG, "Holy crap we're pro!" );
				//Purchase purchase = inventory.getPurchase( PRODUCT_SKU_PRO );
				cacheProLocally();
				m_isPro = true;
			}

			if( !m_isPro )
			{
				Log.d( TAG, "I don't think we're pro :(" );
			}
		}
		catch( IabException e )
		{
			e.printStackTrace();
		}
	}

	@Override
	public void purchasePro()
	{
		m_devPayload = generateNewDevPayload();
		m_billingHelper.launchPurchaseFlow( this, PRODUCT_SKU_PRO, 3, this, m_devPayload );
	}

	@Override
	protected void onActivityResult( final int requestCode, final int resultCode, final Intent data )
	{
		super.onActivityResult( requestCode, resultCode, data );

		if( requestCode == 1001 )
		{
			int responseCode = data.getIntExtra( "RESPONSE_CODE", 0 );
			String purchaseData = data.getStringExtra( "INAPP_PURCHASE_DATA" );
			String dataSignature = data.getStringExtra( "INAPP_DATA_SIGNATURE" );

			if( resultCode == RESULT_OK && responseCode == 0 )
			{
				try
				{
					JSONObject jo = new JSONObject( purchaseData );
					String sku = jo.getString( "productId" );
					String devPayload = jo.getString( "developerPayload" );

					if( PRODUCT_SKU_PRO.equals( sku ) &&
					    Security.verifyPurchase( assemblePublicKey(), purchaseData, dataSignature ) &&
					    m_devPayload.equals( devPayload ) )
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

	private String generateNewDevPayload()
	{
		final Date now = new Date();
		final long fudge = now.getTime() / 7l;
		return BillingSecurity.sha1Hash( fudge + "noh4xplz" );
	}

	private void cacheProLocally()
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( this );
		settings.edit().putBoolean( Preferences.KEY_IS_PRO, true ).commit();
	}

	private String assemblePublicKey()
	{
		return BillingSecurity.superSecureCrypto( EENY ) +
		       BillingSecurity.superSecureCrypto( MEENY ) +
		       BillingSecurity.superSecureCrypto( getString( R.string.miny ) ) +
		       BillingSecurity.superSecureCrypto( getString( R.string.moe ) );
	}
}
