package com.darkrockstudios.apps.randdit.misc;

import java.io.Serializable;

/**
 * Created by Adam on 11/11/13.
 */
public class Post implements Serializable
{
	public String timestamp;
	public String domain;
	public String subredditfk;
	public String id;
	public String title;
	public String url;
	public String author;
	public String created;
	public String permalink;
	public String nsfw;
	public String subreddit;
	public String nsfwsub;
	public String restricted;
	public int    is_image;
}
