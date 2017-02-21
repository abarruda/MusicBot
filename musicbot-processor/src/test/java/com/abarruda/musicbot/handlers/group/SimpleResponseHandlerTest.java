package com.abarruda.musicbot.handlers.group;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.telegram.telegrambots.api.objects.Message;

import com.abarruda.musicbot.config.Configuration;
import com.abarruda.musicbot.items.TermResponse;
import com.abarruda.musicbot.message.TelegramMessage.GroupMessage;
import com.abarruda.musicbot.persistence.DatabaseFacade;
import com.abarruda.musicbot.processor.responder.responses.TextResponse;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;

import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class SimpleResponseHandlerTest {
	
	private SimpleResponseHandler handler;
	@Mock private Configuration mockConfig;
	@Mock private EventBus mockEventBus;
	@Mock private DatabaseFacade mockDb;
	@Mock private Message mockedMessage;
	
	private static final String chatId = "1";
	
	@Before
	public void setUp() {
		when(mockConfig.getConfig(eq(Configuration.AUTORESPONSE_DURATION))).thenReturn("7");
		
		final Map<String, String> chatIds = Maps.newHashMap();
		chatIds.put(chatId, "test-chat");
		when(mockDb.getChatIds()).thenReturn(chatIds);
		
		when(mockedMessage.getChatId()).thenReturn(Long.valueOf(chatId));
		when(mockedMessage.hasText()).thenReturn(true);
		when(mockedMessage.getText()).thenReturn("match1 and match2");
		when(mockedMessage.getChatId()).thenReturn(Long.valueOf(chatId));
	}
	
	@Test
	public void testMultipleResponsesFromOneMessage() throws Exception {
		final TermResponse termResponse1 = new TermResponse(123, new Date(), "match1", "matched1!");
		final TermResponse termResponse2 = new TermResponse(456, new Date(), "match2", "matched2!");
		final List<TermResponse> responses = Lists.newArrayList(termResponse1, termResponse2);
		when(mockDb.getTermResponses(eq(chatId))).thenReturn(responses);
		
		handler = new SimpleResponseHandler(mockConfig, mockEventBus, mockDb);
		
		final GroupMessage testMessage = new GroupMessage(mockedMessage); 
		handler.handleMessage(testMessage);
		
		final ArgumentCaptor<TextResponse> captor = ArgumentCaptor.forClass(TextResponse.class);
		verify(mockEventBus, times(2)).post(captor.capture());
		final TextResponse response1 = captor.getAllValues().get(0);
		assertEquals(chatId, response1.chatId);
		assertEquals("matched2!", response1.text);
		final TextResponse response2 = captor.getAllValues().get(1);
		assertEquals(chatId, response2.chatId);
		assertEquals("matched1!", response2.text);
	}
	
	@Test
	public void testResponseExpiration() throws Exception {
		final TermResponse termResponse1 = new TermResponse(123, new Date(), "match1", "matched1!");
		final Date expiredDate = new Date(new Date().getTime() - Duration.ofDays(8).toMillis());
		final TermResponse termResponse2 = new TermResponse(456, expiredDate, "match2", "matched2!");
		final List<TermResponse> responses = Lists.newArrayList(termResponse1, termResponse2);
		when(mockDb.getTermResponses(eq(chatId))).thenReturn(responses);
		
		handler = new SimpleResponseHandler(mockConfig, mockEventBus, mockDb);
		
		final GroupMessage testMessage = new GroupMessage(mockedMessage); 
		handler.handleMessage(testMessage);
		
		final ArgumentCaptor<TextResponse> captor = ArgumentCaptor.forClass(TextResponse.class);
		verify(mockEventBus, times(1)).post(captor.capture());
		assertEquals("matched1!", captor.getValue().text);
	}

}
