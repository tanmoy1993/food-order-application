package com.demoapp.foodorder.buyer.buyerservice.configuration;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import org.springframework.kafka.support.converter.JsonMessageConverter;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import com.demoapp.foodorder.buyer.buyerservice.model.PreferredContact;
import com.demoapp.foodorder.buyer.buyerservice.service.PreferredTopicProcessor;

@EnableKafka
@Configuration
public class KafkaConf {

    @Value("${KAFKA_ADVERTISED_HOST_NAME}")
    String brokerIp;

    @Value("${KAFKA_ADVERTISED_PORT}")
    int brokerPort;

    @Value("${BUYER_SERVICE_KAFKA_GROUP_ID}")
    String groupId;

    @Value("${KAFKA_TOPIC_NAME_ORDER_CONFIRMED}")
    private String prefContactTopicName;

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerIp + ":" + brokerPort);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(JsonDeserializer.TYPE_MAPPINGS, "ord_cnf:com.demoapp.foodorder.buyer.buyerservice.model.PreferredContact");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "java.util,java.lang,com.demoapp.foodorder.buyer.buyerservice");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        //props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.demoapp.foodorder.buyer.buyerservice.model.PreferredContact");
        //PreferredTopicProcessor.build();
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setAckMode(AckMode.RECORD);
        factory.getContainerProperties().setAckOnError(true);
        //factory.setMessageConverter(new StringJsonMessageConverter());
        return factory;
    }

    @Bean
    public ProducerFactory<String, PreferredContact> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerIp + ":" + brokerPort);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(JsonSerializer.TYPE_MAPPINGS, "user_contact:com.demoapp.foodorder.buyer.buyerservice.model.PreferredContact");
        return new DefaultKafkaProducerFactory<>(configProps);
    }
 
    @Bean
    public KafkaTemplate<String, PreferredContact> kafkaTemplate() {
    	KafkaTemplate<String, PreferredContact> template = new KafkaTemplate<>(producerFactory());
    	//template.setMessageConverter(new StringJsonMessageConverter());
        return template;
    }

 }