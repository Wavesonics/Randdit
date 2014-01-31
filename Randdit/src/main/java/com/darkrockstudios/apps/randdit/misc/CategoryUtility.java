package com.darkrockstudios.apps.randdit.misc;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Adam on 1/30/14.
 */
public class CategoryUtility
{
	private static final String TAG      = CategoryUtility.class.getSimpleName();
	private static final String FILENAME = "categories";

	public static CategoryDefinition findByName( final String name, final Categories categories )
	{
		CategoryDefinition categoryDefinition = null;

		if( categories != null && name != null )
		{
			for( final CategoryDefinition def : categories.categories )
			{
				if( name.equalsIgnoreCase( def.name ) )
				{
					categoryDefinition = def;
				}
			}
		}

		return categoryDefinition;
	}

	public static Categories getCategories( final Context context )
	{
		Categories categories = null;

		String categoriesStr = readFile( context );

		if( categoriesStr.length() > 0 )
		{
			Gson gson = new Gson();
			categories = gson.fromJson( categoriesStr, Categories.class );
		}

		return categories;
	}

	private static String readFile( final Context context )
	{
		StringBuilder sb = new StringBuilder();
		FileInputStream fis = null;
		try
		{
			fis = context.openFileInput( FILENAME );
			BufferedReader reader = new BufferedReader( new InputStreamReader( fis, "UTF-8" ) );
			String line;
			while( (line = reader.readLine()) != null )
			{
				sb.append( line ).append( "\n" );
			}
			fis.close();
		}
		catch( final IOException e )
		{
			Log.d( TAG, "Categories file not found for reading" );
		}
		finally
		{
			if( fis != null )
			{
				try
				{
					fis.close();
				}
				catch( final IOException e1 )
				{
					e1.printStackTrace();
				}
			}
		}

		return sb.toString();
	}

	public static void writeFile( final Categories categories, final Context context )
	{
		final Gson gson = new Gson();
		final String categoriesJson = gson.toJson( categories );

		FileOutputStream fos = null;
		try
		{
			fos = context.openFileOutput( FILENAME, Context.MODE_PRIVATE );
			fos.write( categoriesJson.getBytes() );
		}
		catch( final IOException e )
		{
			Log.d( TAG, "Categories file could not be written" );
		}
		finally
		{
			if( fos != null )
			{
				try
				{
					fos.close();
				}
				catch( final IOException e )
				{
					e.printStackTrace();
				}
			}
		}
	}
}
