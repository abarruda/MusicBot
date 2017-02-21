package com.abarruda.musicbot.handlers.direct;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.User;

import com.abarruda.musicbot.ChatManager;
import com.abarruda.musicbot.config.Configuration;
import com.abarruda.musicbot.handlers.ChatListUtil;
import com.abarruda.musicbot.message.TelegramMessage.PrivateMessage;
import com.abarruda.musicbot.processor.responder.responses.ButtonResponse.Button;
import com.abarruda.musicbot.processor.responder.responses.InlineButtonResponse;
import com.abarruda.musicbot.processor.responder.responses.TextResponse;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;

import static org.mockito.Matchers.anyObject;

import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import static org.junit.Assert.assertEquals;


@RunWith(MockitoJUnitRunner.class)
public class BrowseSetsHandlerTest {
	
	@Mock private EventBus mockEventBus;
	@Mock private ChatManager mockChatManager;
	@Mock private Configuration mockConfig;
	@Mock private User mockUser;
	
	private BrowseSetsHandler handler;
	
	private static final String website = "www.test.com";
	private static final String chatId = "123456";
	private static final int userId = 123;
	
	@Before
	public void setUp() {
		when(mockConfig.getConfig(eq(Configuration.WEBSITE))).thenReturn(website);
		handler = new BrowseSetsHandler(mockEventBus, mockChatManager, mockConfig);
		when(mockUser.getId()).thenReturn(userId);
	}
	
	@Test
	public void testNoOp() {
		final Message mockMessage = mock(Message.class);
		when(mockMessage.hasText()).thenReturn(true);
		when(mockMessage.getText()).thenReturn("Some text");
		final PrivateMessage message = new PrivateMessage(mockMessage);
		
		handler.handleMessage(message);
		
		verify(mockEventBus, never()).post(anyObject());
	}
	
	@Test
	public void testNotActive() {
		
		when(mockChatManager.getChatsForUserFromCache(eq(userId))).thenReturn(Maps.newHashMap());
		
		final Message mockMessage = mock(Message.class);
		when(mockMessage.getChatId()).thenReturn(Long.valueOf(chatId));
		when(mockMessage.getFrom()).thenReturn(mockUser);
		when(mockMessage.hasText()).thenReturn(true);
		when(mockMessage.getText()).thenReturn("Browse Music");
		final PrivateMessage message = new PrivateMessage(mockMessage);
		
		handler.handleMessage(message);
		
		final ArgumentCaptor<TextResponse> captor = ArgumentCaptor.forClass(TextResponse.class);
		verify(mockEventBus, times(1)).post(captor.capture());
		assertEquals(chatId, captor.getValue().chatId);
		assertEquals(ChatListUtil.INACTIVE_USER_TEXT, captor.getValue().text);
	}
	
	@Test
	public void testTwoChatsToSelectFrom() {
		final Map<String, String> chatsForUser = ImmutableMap.of("chat1", "123", "chat2", "456");;
		when(mockChatManager.getChatsForUserFromCache(eq(123))).thenReturn(chatsForUser);
		
		final Message mockMessage = mock(Message.class);
		when(mockMessage.getChatId()).thenReturn(Long.valueOf(chatId));
		when(mockMessage.getFrom()).thenReturn(mockUser);
		when(mockMessage.hasText()).thenReturn(true);
		when(mockMessage.getText()).thenReturn("Browse Music");
		final PrivateMessage message = new PrivateMessage(mockMessage);
		
		handler.handleMessage(message);
		
		final ArgumentCaptor<InlineButtonResponse> captor = ArgumentCaptor.forClass(InlineButtonResponse.class);
		verify(mockEventBus, times(1)).post(captor.capture());
		final InlineButtonResponse response = captor.getValue();
		assertEquals(2, response.buttonTextMapping.size());
		final Button button1 = response.buttonTextMapping.get(0).get(0);
		assertEquals("chat1", button1.buttonText);
		assertEquals("BrowseSetsHandler:::123", button1.buttonData);
		final Button button2 = response.buttonTextMapping.get(1).get(0);
		assertEquals("chat2", button2.buttonText);
		assertEquals("BrowseSetsHandler:::456", button2.buttonData);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testCallbackNoData() {
		final CallbackQuery query = mock(CallbackQuery.class);
		when(query.getData()).thenReturn("");
		handler.handleCallbackQuery(query);
	}
	
	@Test
	public void testCallbackBadData() {
		final CallbackQuery query = mock(CallbackQuery.class);
		when(query.getData()).thenReturn("SomeOtherHandlersData:::data-123");
		handler.handleCallbackQuery(query);
		verify(mockEventBus, never()).post(anyObject());
	}
	
	@Test
	public void testCallback() {
		final Message message = mock(Message.class);
		when(message.getChatId()).thenReturn(Long.valueOf(chatId));
		
		final CallbackQuery query = mock(CallbackQuery.class);
		when(query.getFrom()).thenReturn(mockUser);
		when(query.getMessage()).thenReturn(message);
		when(query.getData()).thenReturn("BrowseSetsHandler:::987");
		
		handler.handleCallbackQuery(query);
		
		final ArgumentCaptor<InlineButtonResponse> captor = ArgumentCaptor.forClass(InlineButtonResponse.class);
		verify(mockEventBus, times(1)).post(captor.capture());
		
		final InlineButtonResponse response = captor.getValue();
		assertEquals(1, response.buttonTextMapping.size());
		final Button button = response.buttonTextMapping.get(0).get(0);
		assertEquals(website + "?chatId=987&userId=123", button.buttonUrl);
	}

}
