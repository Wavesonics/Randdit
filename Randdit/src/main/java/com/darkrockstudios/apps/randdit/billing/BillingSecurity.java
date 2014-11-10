package com.darkrockstudios.apps.randdit.billing;

/**
 * Created by Adam on 12/10/13.
 */

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * BillingSecurity-related methods. For a secure implementation, all of this code
 * should be implemented on a server that communicates with the
 * application on the device. For the sake of simplicity and clarity of this
 * example, this code is included here and is executed on the device. If you
 * must performVerify the purchases on the phone, you should obfuscate this code to
 * make it harder for an attacker to replace the code with stubs that treat all
 * purchases as verified.
 */
public class BillingSecurity
{
	private static final String TAG = BillingSecurity.class.getSimpleName();

	public static String sha1Hash( final String toHash )
	{
		String hash = null;
		try
		{
			MessageDigest digest = MessageDigest.getInstance( "SHA-1" );
			byte[] bytes = toHash.getBytes( "UTF-8" );
			digest.update( bytes, 0, bytes.length );
			bytes = digest.digest();
			StringBuilder sb = new StringBuilder();
			for( byte b : bytes )
			{
				sb.append( String.format( "%02X", b ) );
			}
			hash = sb.toString();
		}
		catch( NoSuchAlgorithmException | UnsupportedEncodingException e )
		{
			e.printStackTrace();
		}

		return hash;
	}

	public static String xorString( final String input, final char[] key )
	{
		final StringBuilder builder = new StringBuilder();
		for( int ii = 0; ii < input.length(); ++ii )
		{
			final int keySegment = key[ ii % key.length ];
			final char c = input.charAt( ii );

			builder.append( c ^ keySegment );
		}

		return builder.toString();
	}

	public static String superSecureCrypto( final String input )
	{
		StringBuilder sb = new StringBuilder();
		for( int i = 0; i < input.length(); i++ )
		{
			char c = input.charAt( i );
			if( c >= 'a' && c <= 'm' )
			{
				c += 13;
			}
			else if( c >= 'A' && c <= 'M' )
			{
				c += 13;
			}
			else if( c >= 'n' && c <= 'z' )
			{
				c -= 13;
			}
			else if( c >= 'N' && c <= 'Z' )
			{
				c -= 13;
			}
			sb.append( c );
		}
		return sb.toString();
	}
}
