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
import com.abarruda.musicbot.handlers.CallbackQueryUtil;
import com.abarruda.musicbot.items.TermResponse;
import com.abarruda.musicbot.message.TelegramMessage.PrivateMessage;
import com.abarruda.musicbot.persistence.DatabaseFacade;
import com.abarruda.musicbot.processor.responder.responses.TextResponse;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class TermResponseInputHandlerTest {
	
	private TermResponseInputHandler handler;
	@Mock private EventBus mockEventBus;
	@Mock private DatabaseFacade mockDb;
	@Mock private ChatManager mockChatManager;

	@Before
	public void setUp() {
		
		handler = new TermResponseInputHandler(mockEventBus, mockDb, mockChatManager);		
	}
	
	private PrivateMessage getMockedPrivateMessage(final User user, final Long directChatId, final String text) {
		final Message telegramMessage = mock(Message.class);
		when(telegramMessage.getFrom()).thenReturn(user);
		when(telegramMessage.getChatId()).thenReturn(directChatId);
		when(telegramMessage.hasText()).thenReturn(true);
		when(telegramMessage.getText()).thenReturn(text);
		return new PrivateMessage(telegramMessage);
	}
	
	private CallbackQuery getMockedCallbackQuery(final User user, final Long directChatId, final String data) {
		final CallbackQuery query = mock(CallbackQuery.class);
		when(query.getFrom()).thenReturn(user);
		when(query.getData()).thenReturn(data);
		final Message queryMessage = mock(Message.class);
		when(queryMessage.getChatId()).thenReturn(directChatId);
		when(query.getMessage()).thenReturn(queryMessage);
		return query;
	}
	
	@Test
	public void testDoNothingWithRegularMessage() {
		final Integer userId = 1;
		final User user = mock(User.class);
		when(user.getId()).thenReturn(userId);
		
		final PrivateMessage message = getMockedPrivateMessage(user, 123L, "hey");
		
		handler.handleMessage(message);
		
		verify(mockEventBus, never()).post(anyObject());
	}
	
	@Test
	public void testUserHasntBeenActiveInChat() {
		final Long userDirectChatId = 11L; 
		final String chat1Id = "chat1";
		
		final User user1 = mock(User.class);
		when(user1.getId()).thenReturn(1);
		
		final PrivateMessage message1 = getMockedPrivateMessage(user1, userDirectChatId, TermResponseInputHandler.AUTO_RESPONDER_COMMAND);		

		handler.handleMessage(message1);
		
		verify(mockEventBus, times(1)).post(anyObject());
		
		final String data = TermResponseInputHandler.class.getSimpleName() + CallbackQueryUtil.DELIMITER + chat1Id;
		final CallbackQuery query1 = getMockedCallbackQuery(user1, userDirectChatId, data);
		
		when(mockChatManager.getChatsForUserFromCache(eq(user1.getId()))).thenReturn(ImmutableMap.of("Some other chat", "chat100"));
		
		handler.handleCallbackQuery(query1);
		
		final ArgumentCaptor<TextResponse> responseCaptor = ArgumentCaptor.forClass(TextResponse.class); 
		verify(mockEventBus, times(2)).post(responseCaptor.capture());
		assertEquals("You must be active in a chat within the last week to perform this action.", responseCaptor.getValue().text);
		assertEquals(userDirectChatId.toString(), responseCaptor.getValue().chatId);
	}
	
	@Test
	public void testAutoresponderAlreadySet() {
		final Integer userId = 1;
		final Long userDirectChatId = 11L; 
		final String chat1Id = "chat1";
		
		final User user1 = mock(User.class);
		when(user1.getId()).thenReturn(userId);
		
		final PrivateMessage message1 = getMockedPrivateMessage(user1, userDirectChatId, TermResponseInputHandler.AUTO_RESPONDER_COMMAND);
		
		handler.handleMessage(message1);
		
		verify(mockEventBus, times(1)).post(anyObject());
		
		final String data = TermResponseInputHandler.class.getSimpleName() + CallbackQueryUtil.DELIMITER + chat1Id;
		final CallbackQuery query1 = getMockedCallbackQuery(user1, userDirectChatId, data);
		
		when(mockChatManager.getChatsForUserFromCache(eq(user1.getId()))).thenReturn(ImmutableMap.of("A fun chat", "chat1"));
		final List<TermResponse> responses = Lists.newArrayList(new TermResponse(userId, new Date(), "term", "response"));
		when(mockDb.getTermResponses(eq(chat1Id))).thenReturn(responses);
		
		handler.handleCallbackQuery(query1);
		
		final ArgumentCaptor<TextResponse> responseCaptor = ArgumentCaptor.forClass(TextResponse.class); 
		verify(mockEventBus, times(2)).post(responseCaptor.capture());
		assertEquals("Sorry, you have already made a submission for this chat, please wait a week!", responseCaptor.getValue().text);
		assertEquals(userDirectChatId.toString(), responseCaptor.getValue().chatId);
	}
	
	@Test
	public void testAddAutoResponsesForMultipleChats() {
		final Integer userId = 1;
		final Long userDirectChatId = 11L; 
		final String chat1Id = "chat1";
		final String chat2Id = "chat2";
		
		when(mockChatManager.getChatsForUserFromCache(eq(userId))).thenReturn(ImmutableMap.of(
				"A fun chat", chat1Id,
				"Another fun chat", chat2Id));
		
		final User user1 = mock(User.class);
		when(user1.getId()).thenReturn(userId);
		
		// First input
		
		final PrivateMessage message1 = getMockedPrivateMessage(user1, userDirectChatId, TermResponseInputHandler.AUTO_RESPONDER_COMMAND);
		
		handler.handleMessage(message1);
		
		verify(mockEventBus, times(1)).post(anyObject());
		
		final String data = TermResponseInputHandler.class.getSimpleName() + CallbackQueryUtil.DELIMITER + chat1Id;
		final CallbackQuery query1 = getMockedCallbackQuery(user1, userDirectChatId, data);
		
		when(mockDb.getTermResponses(eq(chat1Id))).thenReturn(Lists.newArrayList());
		
		handler.handleCallbackQuery(query1);
		
		final ArgumentCaptor<TextResponse> response1Captor = ArgumentCaptor.forClass(TextResponse.class); 
		verify(mockEventBus, times(2)).post(response1Captor.capture());
		assertEquals("What term do you want to look for?", response1Captor.getValue().text);
		assertEquals(userDirectChatId.toString(), response1Captor.getValue().chatId);
		
		final PrivateMessage message2 = getMockedPrivateMessage(user1, userDirectChatId, "term");
		
		handler.handleMessage(message2);
		
		final ArgumentCaptor<TextResponse> response2Captor = ArgumentCaptor.forClass(TextResponse.class);
		verify(mockEventBus, times(3)).post(response2Captor.capture());
		assertEquals("What response do you want to send when 'term' is seen?", response2Captor.getValue().text);
		assertEquals(userDirectChatId.toString(), response1Captor.getValue().chatId);
		
		final PrivateMessage message3 = getMockedPrivateMessage(user1, userDirectChatId, "response");
		
		handler.handleMessage(message3);
		
		final ArgumentCaptor<TermResponse> termResponse1Captor = ArgumentCaptor.forClass(TermResponse.class);
		verify(mockDb, times(1)).insertTermResponse(eq(chat1Id), termResponse1Captor.capture());
		assertEquals("term", termResponse1Captor.getValue().term);
		assertEquals("response", termResponse1Captor.getValue().response);
		assertEquals(userId.intValue(), termResponse1Captor.getValue().userId);
		
		final ArgumentCaptor<TextResponse> response3Captor = ArgumentCaptor.forClass(TextResponse.class);
		verify(mockEventBus, times(4)).post(response3Captor.capture());
		assertEquals("Successfully created!  Wait a minute for the response to become active.", response3Captor.getValue().text);
		assertEquals(userDirectChatId.toString(), response3Captor.getValue().chatId);
		
		// now attempt to add second auto responder to a different chat
		when(mockDb.getTermResponses(eq(chat1Id))).thenReturn(Lists.newArrayList(termResponse1Captor.getValue()));
		when(mockDb.getTermResponses(eq(chat2Id))).thenReturn(Lists.newArrayList());
		
		// Second input
		
		final PrivateMessage message4 = getMockedPrivateMessage(user1, userDirectChatId, TermResponseInputHandler.AUTO_RESPONDER_COMMAND);
		
		handler.handleMessage(message4);
		
		verify(mockEventBus, times(5)).post(anyObject());
		
		final String data2 = TermResponseInputHandler.class.getSimpleName() + CallbackQueryUtil.DELIMITER + chat2Id;
		final CallbackQuery query2 = getMockedCallbackQuery(user1, userDirectChatId, data2);
		
		handler.handleCallbackQuery(query2);
		
		final ArgumentCaptor<TextResponse> response4Captor = ArgumentCaptor.forClass(TextResponse.class); 
		verify(mockEventBus, times(6)).post(response4Captor.capture());
		assertEquals("What term do you want to look for?", response4Captor.getValue().text);
		assertEquals(userDirectChatId.toString(), response4Captor.getValue().chatId);
		
		final PrivateMessage message5 = getMockedPrivateMessage(user1, userDirectChatId, "term2");
		
		handler.handleMessage(message5);
		
		final ArgumentCaptor<TextResponse> response5Captor = ArgumentCaptor.forClass(TextResponse.class);
		verify(mockEventBus, times(7)).post(response5Captor.capture());
		assertEquals("What response do you want to send when 'term2' is seen?", response5Captor.getValue().text);
		assertEquals(userDirectChatId.toString(), response5Captor.getValue().chatId);
		
		final PrivateMessage message6 = getMockedPrivateMessage(user1, userDirectChatId, "response2");
		
		handler.handleMessage(message6);
		
		final ArgumentCaptor<TermResponse> termResponse2Captor = ArgumentCaptor.forClass(TermResponse.class);
		verify(mockDb, times(1)).insertTermResponse(eq(chat2Id), termResponse2Captor.capture());
		assertEquals("term2", termResponse2Captor.getValue().term);
		assertEquals("response2", termResponse2Captor.getValue().response);
		assertEquals(userId.intValue(), termResponse2Captor.getValue().userId);
		
		final ArgumentCaptor<TextResponse> response6Captor = ArgumentCaptor.forClass(TextResponse.class);
		verify(mockEventBus, times(8)).post(response6Captor.capture());
		assertEquals("Successfully created!  Wait a minute for the response to become active.", response6Captor.getValue().text);
		assertEquals(userDirectChatId.toString(), response6Captor.getValue().chatId);
	}
	
}
