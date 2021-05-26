package com.demoapp.foodorder.restaurant.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.demoapp.foodorder.restaurant.model.Restaurant;

import reactor.core.publisher.Flux;

@Repository
public interface RestaurantRepo extends ReactiveMongoRepository<Restaurant, String>{
	
	Flux<Restaurant> findAllByUserId(String userId);

}
