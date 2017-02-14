package com.abarruda.musicbot.handlers.direct;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;

import com.abarruda.musicbot.ChatManager;
import com.abarruda.musicbot.config.Configuration;
import com.abarruda.musicbot.handlers.CallbackQueryUtil;
import com.abarruda.musicbot.handlers.CallbackQueryUtil.CallbackQueryInfo;
import com.abarruda.musicbot.handlers.ChatListUtil;
import com.abarruda.musicbot.message.TelegramMessage;
import com.abarruda.musicbot.processor.responder.responses.InlineButtonResponse;
import com.abarruda.musicbot.processor.responder.responses.InlineButtonResponse.InlineButtonResponseBuilder;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

public class BrowseSetsHandler {
	
	private static final Logger logger = LogManager.getLogger(BrowseSetsHandler.class);
	
	public static final String COMMAND_BROWSE_MUSIC = "Browse Music";
	
	private final EventBus eventBus;
	private final ChatManager chatManager;
	private final Configuration configuration;
	
	@Inject
	public BrowseSetsHandler(final EventBus eventBus, final ChatManager chatManager, final Configuration configuration) {
		this.eventBus = eventBus;
		this.chatManager = chatManager;
		this.configuration = configuration;
	}

	@Subscribe
	public void handleMessage(final TelegramMessage.PrivateMessage privateMessage) {
		final Message message = privateMessage.getMessage();
		if (message.hasText() && message.getText().equals(COMMAND_BROWSE_MUSIC)) {
			final int userId = message.getFrom().getId();
			final String directMessageChatId = message.getChatId().toString();
			eventBus.post(ChatListUtil.getChatListForUser(chatManager, directMessageChatId, userId, 
					"Select the chat you want to browse:",
					BrowseSetsHandler.class.getSimpleName()));
		}
	}

	@Subscribe
	public void handleCallbackQuery(final CallbackQuery query) {
		final CallbackQueryInfo info = CallbackQueryUtil.getInfo(query);
		
		if (info.source.equals(BrowseSetsHandler.class.getSimpleName())) {
			
			final int userId = query.getFrom().getId();
			final String directChatId = query.getMessage().getChatId().toString();
			final String chatIdFromButton = info.data;
			
			final StringBuilder description = new StringBuilder();
			description.append("Click the button to start browsing music!\n");
			description.append("\n");
			description.append("`Pro Tip:` \n");
			description.append("iOS devices: Once the webpage loads, open in Safari and click the 'share' button, " +
			"then select 'Add to Home Screen' button to install your customized app for quick reference.");
			
			final InlineButtonResponseBuilder builder = new InlineButtonResponseBuilder()
					.setChatId(directChatId)
					.setSilent(false)
					.setText(description.toString());
			
			final String url = String.format(this.configuration.getConfig(Configuration.WEBSITE) + "?chatId=%s&userId=%s", chatIdFromButton, userId);
			
			builder.addButtonRow(InlineButtonResponseBuilder.newButtonWithUrl("Click here to browse music", url));
			
			logger.info("User '" + userId + "' (" + query.getFrom().getFirstName() + " " + query.getFrom().getLastName() + 
					") requested to browse music in chat '" + chatIdFromButton + "'");
			
			eventBus.post(InlineButtonResponse.createResponse(builder));
		}
	}

}
