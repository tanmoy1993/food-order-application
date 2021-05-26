package com.demoapp.foodorder.buyer.buyerservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.demoapp.foodorder.buyer.buyerservice.model.PreferredContact;

@Service
public class PreferredTopicListener {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PreferredTopicListener.class);

    @KafkaListener(topics = "${KAFKA_TOPIC_NAME_ORDER_CONFIRMED}",
                               groupId = "${BUYER_SERVICE_KAFKA_GROUP_ID}", 
                               containerFactory = "kafkaListenerContainerFactory",
                               autoStartup = "${listen.auto.start:true}", 
                               concurrency = "${listen.concurrency:2}")
	public void consumeMessage(@Payload PreferredContact pc) {
    	LOGGER.info("Consumed Message: " + pc);
    	PreferredTopicProcessor.submitTopic(pc);
		return;
    }

}
