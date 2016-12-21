package com.abarruda.musicbot.items;

import java.net.URL;
import java.util.List;

import com.google.common.collect.Lists;

public enum ContentType {
	
	SOUNDCLOUD("soundcloud.com", "www.soundcloud.com"),
	YOUTUBE("youtube.com", "www.youtube.com", "youtu.be"),
	MISC("");
	
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

}
