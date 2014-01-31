package com.darkrockstudios.apps.randdit.misc;

import java.io.Serializable;

/**
 * Created by Adam on 1/30/14.
 */
public class CategoryDefinition implements Serializable
{
	public String name;
	public String subredditcsv;
	public int    is_sfw;

	public CategoryDefinition()
	{

	}

	public CategoryDefinition( final String name, final String subredditcsv, final int is_sfw )
	{
		this.name = name;
		this.subredditcsv = subredditcsv;
		this.is_sfw = is_sfw;
	}
}
