package com.demoapp.foodorder.buyer.buyerservice.configuration;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableAutoConfiguration
@EntityScan(basePackages = {"com.demoapp.foodorder.buyer.buyerservice"})
@EnableJpaRepositories(basePackages = {"com.demoapp.foodorder.buyer.buyerservice.repository"})
@EnableTransactionManagement
public class DatabaseConf{
	
}
