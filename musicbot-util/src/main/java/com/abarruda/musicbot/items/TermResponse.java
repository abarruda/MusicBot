package com.abarruda.musicbot.items;

import java.util.Date;

import org.bson.Document;

public class TermResponse {
	public String _id;
	public int userId;
	public Date date;
	public String term;
	public String response;
	
	public TermResponse(int userId, Date date, String term, String response) {
		this.userId = userId;
		this.date = date;
		this.term = term;
		this.response = response;
	}
	
	public static TermResponse getTermResponseFromDoc(Document doc) {
		TermResponse termResponse = new TermResponse(
				doc.getInteger("userId"), 
				doc.getDate("date"),
				doc.getString("term"),
				doc.getString("response"));
		termResponse._id = doc.getObjectId("_id").toString();
		return termResponse;
	}
	
}
