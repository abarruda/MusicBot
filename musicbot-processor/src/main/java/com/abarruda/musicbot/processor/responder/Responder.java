package com.abarruda.musicbot.processor.responder;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.abarruda.musicbot.processor.Respondable;
import com.abarruda.musicbot.processor.responder.responses.BotResponse;
import com.abarruda.musicbot.processor.responder.responses.InlineButtonResponse;
import com.abarruda.musicbot.processor.responder.responses.TextButtonResponse;
import com.abarruda.musicbot.processor.responder.responses.TextResponse;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class Responder implements Runnable {
	
	private static final Logger logger = LogManager.getLogger(Responder.class);
	
	private ConcurrentLinkedQueue<BotResponse> queueOfWork;
	private Respondable telegramBot;
	private ScheduledExecutorService executor;
	
	private Responder(ConcurrentLinkedQueue<BotResponse> queueOfWork, Respondable telegramBot) {
		this.queueOfWork = queueOfWork;
		this.telegramBot = telegramBot;
		final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("responder-pool-thread-%d").build();
		this.executor = Executors.newScheduledThreadPool(5, threadFactory);
	}
	
	public static Responder initializeResponder(ConcurrentLinkedQueue<BotResponse> queue, Respondable telegramBot) {
		return new Responder(queue, telegramBot);
	}

	public void start() {
		executor.scheduleAtFixedRate(this, 0, 500, TimeUnit.MILLISECONDS);
	}
	
	public void stop() {
		
	}

	@Override
	public void run() {
		try {
			BotResponse response = queueOfWork.poll();
			
			if (response != null) {
				if (response instanceof TextResponse) {
					telegramBot.respondWithTextMessage((TextResponse)response);
				} else if (response instanceof TextButtonResponse) {
					telegramBot.respondWithButtonMessage((TextButtonResponse)response);
				} else if (response instanceof InlineButtonResponse) {
					telegramBot.respondWithInlineButtonMessage((InlineButtonResponse)response);
				}
			}
		} catch (Exception e) {
			logger.error("Cannot send response!", e);
		}
		
	}
	
	
}
