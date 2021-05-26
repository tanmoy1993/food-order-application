package com.demoapp.foodorder.restaurant.service;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.demoapp.foodorder.restaurant.misc.ServerException;
import com.demoapp.foodorder.restaurant.model.FoodItem;
import com.demoapp.foodorder.restaurant.model.NotificationTemplate;
import com.demoapp.foodorder.restaurant.model.OrderCancelledTopic;
import com.demoapp.foodorder.restaurant.model.OrderReceivedTopic;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;

@Slf4j
@Service
public class OrderProcessor {
	
	private static BlockingQueue<OrderReceivedTopic> orderReceivedTopicQueue = null;
	private static BlockingQueue<OrderCancelledTopic> orderCancelledTopicQueue = null;
		
	@Autowired
	MenuService menuService;
	
	@Autowired
	RestaurantService restaurantService;
	
	@Value("${KAFKA_TOPIC_NAME_NOTIFY_X}")
    private String topic;
	
	@Autowired
	KafkaTemplate<String, NotificationTemplate> template;
	
	public OrderProcessor() {
		orderReceivedTopicQueue = new LinkedBlockingQueue<OrderReceivedTopic>();
		orderCancelledTopicQueue = new LinkedBlockingQueue<OrderCancelledTopic>();
		
		Executors.newFixedThreadPool(2).execute(new ProcessOrderReceivedTopic());
		Executors.newFixedThreadPool(2).execute(new ProcessOrderCancelledTopic());
				
	}
	
	public static void submitOrderReceivedTopic(OrderReceivedTopic o) {
		orderReceivedTopicQueue.add(o);
	}
	
	public static void submitOrderCancelledTopic(OrderCancelledTopic o) {
		orderCancelledTopicQueue.add(o);
	}
	
	class ProcessOrderReceivedTopic implements Runnable{

		@Override
		public void run() {
			Flux.generate((SynchronousSink<OrderReceivedTopic> sink) -> {
				try {
					sink.next(orderReceivedTopicQueue.take());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			})
				.doOnNext(o -> log.info("Processing received order: {}", o))
				.filterWhen(o -> menuService.verifyPrice(o.getOrderedItems(), o.getTotalCost()))
				.doOnError(e -> log.error("Exception during verifying price: {}", e))
				.flatMap(o -> getRestaurantTemplate(o.getRestaurantId(), 
						"received:" + o.getOrderId(), o.getOrderedItems()))
				.map(n -> template.send(topic, n)).subscribe();
		}
	}
	
	class ProcessOrderCancelledTopic implements Runnable{

		@Override
		public void run() {
			Flux.generate((SynchronousSink<OrderCancelledTopic> sink) -> {
				try {
					sink.next(orderCancelledTopicQueue.take());
				} catch (InterruptedException e) {
					log.error("Cancelled topic processor interrupted: {}", e);
				}
			})
				.doOnNext(o -> log.info("Processing cancelled order: {}", o))
				.filter(o -> o.getReason().contains("USER_CANCELLED"))
				.flatMap(o -> getRestaurantTemplate(o.getRestaurantId(), 
						"cancelled:" + o.getOrderId(), null))
				.map(n -> template.send(topic, n)).subscribe();
		}
		
	}
	
	private Mono<NotificationTemplate> getRestaurantTemplate(String resId, String context, List<FoodItem> orderedItems) {
		
		return restaurantService.fetchRestaurant(resId).map(res -> {
			NotificationTemplate template = new NotificationTemplate();
			template.setSubject(context);
			template.setOrderedItems(orderedItems);
			if(res.getEmail()!=null && !res.getEmail().isEmpty()) {
				template.setContactMethod("email");
				template.setContactDetails(res.getEmail());
			} else {
				template.setContactMethod("phone");
				template.setContactDetails(res.getPhone());
			}
			log.info("template: {}", template);
			return template;
		}).switchIfEmpty(Mono.error(new ServerException("Restaurant contact details not found.")));
	}

}
