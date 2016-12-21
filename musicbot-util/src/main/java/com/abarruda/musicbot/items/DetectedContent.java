package com.abarruda.musicbot.items;

public class DetectedContent {
	public ContentType type;
	public String url;
	public int date;
	public User user;
	
	public DetectedContent(final ContentType type, final String url, final int date, final User user) {
		this.type = type;
		this.url = url;
		this.date = date;
		this.user = user;
	}
}

