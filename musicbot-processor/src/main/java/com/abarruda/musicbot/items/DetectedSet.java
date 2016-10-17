package com.abarruda.musicbot.items;

import org.telegram.telegrambots.api.objects.User;

public class DetectedSet {
	public SetType type;
	public String url;
	public int date;
	public User user;
	
	public DetectedSet(final SetType type, final String url, final int date, final User user) {
		this.type = type;
		this.url = url;
		this.date = date;
		this.user = user;
	}
}

