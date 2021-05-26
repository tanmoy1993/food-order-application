package com.demoapp.foodorder.restaurant.controller;

import static com.demoapp.foodorder.restaurant.misc.HelperUtils.error;
import static com.demoapp.foodorder.restaurant.misc.HelperUtils.okBody;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.demoapp.foodorder.restaurant.misc.HelperUtils;
import com.demoapp.foodorder.restaurant.misc.ServerException;
import com.demoapp.foodorder.restaurant.model.ExceptionResponse;
import com.demoapp.foodorder.restaurant.model.Restaurant;
import com.demoapp.foodorder.restaurant.model.User;
import com.demoapp.foodorder.restaurant.service.RestaurantService;
import com.demoapp.foodorder.restaurant.validator.RestaurantValidator;
import com.demoapp.foodorder.restaurant.validator.UserValidator;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
public class RestaurantController {
	
	@Autowired
	RestaurantService restaurantService;
		
	public Mono<ServerResponse> check(ServerRequest request) {
		log.info("check hit!!!");
		return ServerResponse.ok()
				.contentType(MediaType.TEXT_PLAIN)
				.body(Mono.just("check: " + System.currentTimeMillis()), String.class);
	}

	//create user
	public Mono<ServerResponse> createUser(ServerRequest request) {
		Mono<User> user = request.bodyToMono(User.class)
									.transform(UserValidator::validateUser);
		return restaurantService.createUser(user)
				.doOnError(e -> error(e))
				.flatMap(u -> okBody().bodyValue(u));
	}
	
	//login
	public Mono<ServerResponse> userLogin(ServerRequest request) {
		Mono<User> user = request.bodyToMono(User.class)
									.transform(UserValidator::validateUserMinimal);
		return restaurantService.validateAndFetchUser(user)
				.doOnError(e -> error(e))
				.flatMap(u -> okBody().bodyValue(u));
	}
	
	//create restaurant
	public Mono<ServerResponse> createRestaurant(ServerRequest request) {
		Mono<Restaurant> restaurant = request.bodyToMono(Restaurant.class)
						.transformDeferred(RestaurantValidator::validateRestaurantMinimal);
		return restaurant.flatMap(restaurantService::createRestaurant)
				.doOnError(e -> error(e))
				.flatMap(r -> okBody().bodyValue(r));
	}
	
	//get restaurant by user
	public Mono<ServerResponse> getRestaurantByUser(ServerRequest request) {
		Mono<String> userId = Mono.just(request.pathVariable("uid"))
									.transformDeferred(HelperUtils::isNotNullOrEmpty);
		return userId.flatMapMany(restaurantService::fetchRestaurantsUser)
					.doOnError(e -> error(e))
				     .collectList()
					 .flatMap(resList -> okBody().bodyValue(resList));
	}
	
	//get restaurant by zip
	public Mono<ServerResponse> getRestaurantByZip(ServerRequest request) {
		Mono<String> zip = Mono.just(request.pathVariable("zip"))
								.transformDeferred(HelperUtils::isNotNullOrEmpty);
		return zip.map(Integer::parseInt).doOnError(e -> error(new ServerException("Invalid zip code.")))
					.flatMapMany(restaurantService::fetchRestaurantsZip)
					.doOnError(e -> error(e))
				     .collectList()
					 .flatMap(resList -> okBody().bodyValue(resList));
	}
		
	//update restaurant
	public Mono<ServerResponse> updateRestaurant(ServerRequest request) {
		Mono<Restaurant> restaurant = request.bodyToMono(Restaurant.class)
				.transformDeferred(RestaurantValidator::validateRestaurant);
		return restaurant.flatMap(restaurantService::updateRestaurant)
				.doOnError(e -> error(e))
				.flatMap(r -> okBody().bodyValue(r));
	}
	
	//delete restaurant
	public Mono<ServerResponse> deleteRestaurant(ServerRequest request) {
		Mono<Restaurant> restaurant = request.bodyToMono(Restaurant.class)
				.transformDeferred(RestaurantValidator::validateRestaurant);
		return restaurant.flatMap(restaurantService::deleteRestaurant)
				.doOnError(e -> error(e))
				.flatMap(o -> okBody().bodyValue("{\"message\" : \"restaurant deleted.\"}"));
	}
	
	public static Mono<ServerResponse> exceptionProcessor(Throwable e, ServerRequest request){
		log.info("exceptionProcessor hit!!!");
		return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
				.body(ExceptionResponse.buildExceptionResponse(e), ExceptionResponse.class);
	}

}
