package com.demoapp.foodorder.restaurant.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.demoapp.foodorder.restaurant.model.FoodItem;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface FoodItemRepo extends ReactiveMongoRepository<FoodItem, String>{
	
	Flux<FoodItem> findByRestaurantId(String restaurantId);
	
	Mono<Long> deleteByRestaurantId(String restaurantId);

}
