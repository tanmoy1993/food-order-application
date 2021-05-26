package com.demoapp.foodorder.restaurant.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.demoapp.foodorder.restaurant.model.NearbyRestaurants;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface NearbyRestaurantRepo extends ReactiveMongoRepository<NearbyRestaurants, String> {
	
	Flux<NearbyRestaurants> findAllByZip(int zip);
	
	Mono<NearbyRestaurants> findByRestaurantIdAndZip(String restaurantId, int zip);

}
