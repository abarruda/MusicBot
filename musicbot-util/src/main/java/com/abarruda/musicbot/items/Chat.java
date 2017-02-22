package com.abarruda.musicbot.items;

import org.bson.Document;

public class Chat {
	
	public final static String FIELD_CHAT_ID = "chatId";
	public final static String FIELD_NAME = "chatName";
	
	public final String _id;
	public final String chatId;
	public final String name;
	
	public Chat(final String id, final String chatId, final String name) {
		this._id = id;
		this.chatId = chatId;
		this.name = name;
	}
	
	public static Chat getChatFromDoc(final Document doc) {
		
		return new Chat(
				doc.getObjectId("_id").toString(), 
				doc.getString(FIELD_CHAT_ID),
				doc.getString(FIELD_NAME));
	}
	

}
