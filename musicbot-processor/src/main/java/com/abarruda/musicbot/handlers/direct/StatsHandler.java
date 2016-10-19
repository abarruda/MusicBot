package com.abarruda.musicbot.handlers.direct;

import java.util.Map;
import java.util.concurrent.Callable;

import org.telegram.telegrambots.api.objects.Message;

import com.abarruda.musicbot.handlers.CommandUtil;
import com.abarruda.musicbot.handlers.MessageHandler;
import com.abarruda.musicbot.handlers.CommandUtil.Command;
import com.abarruda.musicbot.items.TermResponse;
import com.abarruda.musicbot.persistence.DatabaseFacade;
import com.abarruda.musicbot.persistence.MongoDbFacade;
import com.abarruda.musicbot.processor.responder.responses.BotResponse;
import com.abarruda.musicbot.processor.responder.responses.TextResponse;
import com.google.common.base.Strings;

public class StatsHandler implements MessageHandler {
	
	private static final String COMMAND = "/stats";
	private final DatabaseFacade db = MongoDbFacade.getMongoDb();

	@Override
	public Callable<BotResponse> handleMessage(Message message) {
		return new Callable<BotResponse>() {

			@Override
			public BotResponse call() throws Exception {
				if (message.hasText()) {
					final Command command = CommandUtil.getCommandFromMessage(message);
					
					if (command.getCommand().equals(COMMAND)) {
						final StringBuilder responseString = new StringBuilder();
						
						for(final Map.Entry<String, String> chatIdToName : db.getChatIds().entrySet()) {
							final String chatId = chatIdToName.getKey();
							final String chatName = chatIdToName.getValue();
							
							responseString.append("`" + chatName + "`\n\n");
							responseString.append("*Auto Responses:*\n");
							
							for (final TermResponse tr : db.getTermResponses(chatId)) {
								responseString.append("_" + tr.userId + ":_\n");
								responseString.append(tr.term + " : " + tr.response);
								responseString.append("\n");
							}
							
							responseString.append("\n*Music Sets:*\n");
							responseString.append("# Sets: " + db.getSetCount(chatId));
						}
						
						if (Strings.isNullOrEmpty(responseString.toString())) {
							responseString.append("No information to display");
						}
						
						return TextResponse.createResponse(
								message.getChatId().toString(), 
								responseString.toString(), 
								false,
								true);
					}
				}
				return null;
			}
			
		};
	}

}
