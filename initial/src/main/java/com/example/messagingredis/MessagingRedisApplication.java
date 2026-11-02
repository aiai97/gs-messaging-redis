package com.example.messagingredis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;


@SpringBootApplication
public class MessagingRedisApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(MessagingRedisApplication.class);

	// connect to the redis server and listen for messages on the "chat" topic
	@Bean
	RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
											MessageListenerAdapter listenerAdapter) {

		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.addMessageListener(listenerAdapter, new PatternTopic("chat"));
		container.addMessageListener(listenerAdapter, new PatternTopic("leslie"));

		return container;
	}

	// create a message listener adapter for the receiver
	@Bean
	MessageListenerAdapter listenerAdapter(Receiver receiver) {
		return new MessageListenerAdapter(receiver, "receiveMessage");
	}

	// what to do when a message is received
	@Bean
	Receiver receiver() {
		return new Receiver();
	}

	@Bean
	StringRedisTemplate template(RedisConnectionFactory connectionFactory) {
		return new StringRedisTemplate(connectionFactory);
	}

	public static void main(String[] args) throws InterruptedException {

		ApplicationContext ctx = SpringApplication.run(MessagingRedisApplication.class, args);

		StringRedisTemplate template = ctx.getBean(StringRedisTemplate.class);
		Receiver receiver = ctx.getBean(Receiver.class);

		while (receiver.getCount() == 0) {

			LOGGER.info("Sending message...");
			template.convertAndSend("chat", "Hello from Redis!");
			template.convertAndSend("leslie", "Hello from leslie!");
			Thread.sleep(500L);
		}

		System.exit(0);
	}
}
