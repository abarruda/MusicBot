package com.abarruda.musicbot.handlers.direct;

import java.util.Map;

import org.telegram.telegrambots.api.objects.Message;

import com.abarruda.musicbot.handlers.CommandUtil;
import com.abarruda.musicbot.handlers.CommandUtil.Command;
import com.abarruda.musicbot.items.TermResponse;
import com.abarruda.musicbot.message.TelegramMessage;
import com.abarruda.musicbot.persistence.DatabaseFacade;
import com.abarruda.musicbot.processor.responder.responses.TextResponse;
import com.google.common.base.Strings;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

public class StatsHandler {
	
	private static final String COMMAND = "/stats";
	
	private final EventBus eventBus;
	private final DatabaseFacade db;
	
	@Inject
	public StatsHandler(final EventBus eventBus, final DatabaseFacade db) {
		this.eventBus = eventBus;
		this.db = db;
	}

	@Subscribe
	public void handleMessage(TelegramMessage.PrivateMessage privateMessage) {
		final Message message = privateMessage.getMessage();
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
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
							responseString.append("# Sets: " + db.getRemoteContentCount(chatId));
						}
						
						if (Strings.isNullOrEmpty(responseString.toString())) {
							responseString.append("No information to display");
						}
						
						eventBus.post(TextResponse.createResponse(
								message.getChatId().toString(), 
								responseString.toString(), 
								false,
								true));
					}
				}
			}
		}).start();
		
	}

}
