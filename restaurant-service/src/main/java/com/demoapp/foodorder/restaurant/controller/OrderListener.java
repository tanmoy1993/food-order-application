package com.demoapp.foodorder.restaurant.controller;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.demoapp.foodorder.restaurant.model.OrderCancelledTopic;
import com.demoapp.foodorder.restaurant.model.OrderReceivedTopic;
import com.demoapp.foodorder.restaurant.service.OrderProcessor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OrderListener {

	@KafkaListener(id = "rec", topics = "${KAFKA_TOPIC_NAME_ORDER_RECEIVED}", 
							groupId = "${RESTAURANT_SERVICE_KAFKA_GROUP_ID}", 
							containerFactory = "kafkaListenerContainerFactory", 
							autoStartup = "true", 
							concurrency = "${listen.concurrency:2}")
	public void consumeReceivedMessage(@Payload OrderReceivedTopic rec) {
		log.info("Consumed order received Message: " + rec);
		OrderProcessor.submitOrderReceivedTopic(rec);
		return;
	}

	@KafkaListener(id = "can", topics = "${KAFKA_TOPIC_NAME_ORDER_CANCELLED}", 
						groupId = "${RESTAURANT_SERVICE_KAFKA_GROUP_ID}", 
						containerFactory = "kafkaListenerContainerFactory", 
						autoStartup = "true", concurrency = "${listen.concurrency:2}")
	public void consumeCancelMessage(@Payload OrderCancelledTopic can) {
		log.info("Consumed order cancelled Message: " + can);
		OrderProcessor.submitOrderCancelledTopic(can);
		return;
	}

}
