package com.demoapp.foodorder.restaurant.configuration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@SpringBootApplication
@ComponentScan(basePackages={"com.demoapp.foodorder.restaurant"})
@Configuration
@EnableAutoConfiguration
@EnableReactiveMongoRepositories(basePackages = "com.demoapp.foodorder.restaurant")
public class RestaurantServiceApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(RestaurantServiceApplication.class, args);
	}

}
