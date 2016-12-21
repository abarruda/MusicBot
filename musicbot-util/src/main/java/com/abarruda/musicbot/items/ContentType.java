package com.abarruda.musicbot.items;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import com.google.common.collect.Lists;

public enum ContentType {
	
	SOUNDCLOUD("soundcloud.com", "www.soundcloud.com"),
	YOUTUBE("youtube.com", "www.youtube.com", "youtu.be"),
	MISC(""),
	MALFORMED;
	
	public List<String> hostNames;
	
	ContentType(String... hostnames) {
		hostNames = Lists.newArrayList(hostnames);
	}
	
	public boolean isOfType(final URL url) {
		for (String hostname : hostNames) {
			if (url.getHost().equals(hostname)) {
				return true;
			}
		}
		return false;
	}
	
	public static ContentType determineContentType(final String urlString) {
		URL url;
		try {
			url = new URL(urlString);
			for (ContentType type : ContentType.values()) {
				if (type.isOfType(url)) {
					return type;
				}
			}
			return ContentType.MISC;
		} catch (MalformedURLException e) {
			return ContentType.MALFORMED;
		}
		
	}
	
	public static boolean isMusicSet(final String type) {
		try {
			final List<ContentType> musicContentTypes = Lists.newArrayList(SOUNDCLOUD, YOUTUBE);
			return musicContentTypes.contains(ContentType.valueOf(type));
		} catch (IllegalArgumentException | NullPointerException e) {
			return false;
		}
	}

}
