package com.demoapp.foodorder.buyer.buyerservice.service;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Controller;

import com.demoapp.foodorder.buyer.buyerservice.model.PreferredContact;

@Controller
public class PreferredTopicProcessor {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PreferredTopicProcessor.class);

	private static Queue<PreferredContact> topicQueue = null;
	private static PreferredTopicProcessor INSTANCE = null;
	private static final int NUM_THREADS = 3;
	private static final int SLEEP_INTERVAL = 1000;
	
	@Autowired
	BuyerService buyerService;
	
	@Autowired
	KafkaTemplate<String, PreferredContact> template;
	
	@Value("${KAFKA_TOPIC_NAME_PREFERRED_CONTACT}")
    private String topic;
	
	private PreferredTopicProcessor() {
		topicQueue = new ConcurrentLinkedQueue<PreferredContact>();

		ExecutorService tpool = Executors.newFixedThreadPool(NUM_THREADS);
		for(int i=0; i<NUM_THREADS; i++)
			tpool.execute(new ProcessTopic());
		
	}
	
	@PostConstruct
	public static void build() {
		if(INSTANCE==null)
			INSTANCE = new PreferredTopicProcessor();
	}
	
	public static void submitTopic(PreferredContact pc) {
		topicQueue.add(pc);
	}
	
	class ProcessTopic implements Runnable{

		@Override
		public void run() {
			while(true) {
				PreferredContact pc = topicQueue.poll();
				if(pc!=null) {
					try {
						LOGGER.info("Processing Message: " + pc);
						buyerService.getPreferredContactPreferredContact(pc);
						LOGGER.info("Processed Message: " + pc);
						template.send(topic, pc);
					}  catch (Exception e) {
						LOGGER.error("Error while processing contact request: {}, {}", pc, e);
					}
				} else {
					try {
						Thread.sleep(SLEEP_INTERVAL);
						LOGGER.debug("No contact topic to process. sleeping ...");
					} catch (InterruptedException e) {
						LOGGER.error("Topic processor thread {} interrupted ...", 
								Thread.currentThread().getName() );
					}
				}
			}
		}
		
	}
}
