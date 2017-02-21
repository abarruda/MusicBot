package com.abarruda.musicbot.handlers.direct;

import org.telegram.telegrambots.api.objects.Message;

import com.abarruda.musicbot.handlers.CommandUtil;
import com.abarruda.musicbot.handlers.CommandUtil.Command;
import com.abarruda.musicbot.message.TelegramMessage;
import com.abarruda.musicbot.processor.responder.responses.TextButtonResponse;
import com.abarruda.musicbot.processor.responder.responses.TextButtonResponse.TextButtonResponseBuilder;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

public class HelpMessageHandler {

	private static final ImmutableSet<String> HELP_COMMANDS = ImmutableSet.<String>of("/help", "help");
	
	private static final String HELP_MESSAGE = "*Commands*\n\n" + 
			"`" + TermResponseInputHandler.AUTO_RESPONDER_COMMAND + "`\n" + 
			"Provide a response for the specified (case insensitive) term.  You are allowed one response and it will expire after 1 week.\n" + 
			"\n" + 
			"`" + BrowseSetsHandler.COMMAND_BROWSE_MUSIC + "`\n" + 
			"Get a customized link to browse and play the music detected in the groups you belong to.\n" + 
			"\n" + 
			"`" + FeedbackHandler.FEEDBACK_COMMAND + "`\n" + 
			"Report a bug with the bot or request a feature!";
	
	private static final Predicate<Message> HELP_MESSAGE_PREDICATE = new Predicate<Message>() {
		@Override
		public boolean apply(Message input) {
			if (input.hasText()) {
				final Command command = CommandUtil.getCommandFromMessage(input);
				for (String text: HELP_COMMANDS) {
					if (command.getCommand().toLowerCase().equals(text)) {
						return true;
					}
				}
			}
			return false;
		}
	};
	
	private final EventBus eventBus;
	
	@Inject
	public HelpMessageHandler(final EventBus eventBus) {
		this.eventBus = eventBus;
	}
	
	@Subscribe
	public void handleMessage(final TelegramMessage.PrivateMessage privateMessage) {
		final Message message = privateMessage.getMessage();
		
		if (HELP_MESSAGE_PREDICATE.apply(message)) {
			final TextButtonResponseBuilder builder = new TextButtonResponseBuilder()
					.setChatId(message.getChatId().toString())
					.setSilent(true)
					.setText(HELP_MESSAGE)
					.addButtonRow(TextButtonResponseBuilder.newButton(TermResponseInputHandler.AUTO_RESPONDER_COMMAND))
					.addButtonRow(TextButtonResponseBuilder.newButton(BrowseSetsHandler.COMMAND_BROWSE_MUSIC))
					.addButtonRow(TextButtonResponseBuilder.newButton(FeedbackHandler.FEEDBACK_COMMAND));
			
			eventBus.post(TextButtonResponse.createResponse(builder));  
		}

	}

}
