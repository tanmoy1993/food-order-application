package com.demoapp.foodorder.restaurant.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.demoapp.foodorder.restaurant.model.User;

import reactor.core.publisher.Mono;

@Repository
public interface UserRepo extends ReactiveMongoRepository<User, String>{
	
	public Mono<User> findFirstByUserId(String userId);
	
	public Mono<User> findFirstByUserIdAndPasswordHash(String userId, String passwordHash);

}
