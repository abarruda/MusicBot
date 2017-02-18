package com.abarruda.musicbot.handlers.group;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.telegram.telegrambots.api.objects.Message;

import com.abarruda.musicbot.config.Configuration;
import com.abarruda.musicbot.persistence.DatabaseFacade;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;

import static org.mockito.Mockito.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SimpleResponseHandlerTest {
	
	@Mock private Configuration mockConfig;
	@Mock private EventBus mockEventBus;
	@Mock private DatabaseFacade mockDb;
	
	@Test
	public void testMultipleResponsesFromOneMessage() throws Exception {
		final String chatId = "1";
		
		final Message message = mock(Message.class);
		when(message.getChatId()).thenReturn(Long.valueOf(chatId));
		when(message.hasText()).thenReturn(true);
		when(message.getText()).thenReturn("match1 and match2");
		when(message.getChatId()).thenReturn(Long.valueOf(chatId));
		
		final Map<String, Map<String, String>> mapping = Maps.newHashMap();
		final Map<String, String> termMapping = Maps.newHashMap();
		termMapping.put("match1", "matched1!");
		termMapping.put("match2", "matched2!");
		mapping.put(chatId, termMapping);
		
		final SimpleResponseHandler.HandleMessageRunnable handler = new SimpleResponseHandler.HandleMessageRunnable(message, mockEventBus, mapping);
		handler.run();
		verify(mockEventBus, times(2)).post(anyObject());
		
	}

}
