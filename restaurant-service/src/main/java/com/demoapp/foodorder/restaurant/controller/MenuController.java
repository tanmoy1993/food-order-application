package com.demoapp.foodorder.restaurant.controller;

import static com.demoapp.foodorder.restaurant.misc.HelperUtils.error;
import static com.demoapp.foodorder.restaurant.misc.HelperUtils.okBody;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.demoapp.foodorder.restaurant.misc.HelperUtils;
import com.demoapp.foodorder.restaurant.model.FoodItem;
import com.demoapp.foodorder.restaurant.service.MenuService;
import com.demoapp.foodorder.restaurant.validator.FoodItemValidator;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
public class MenuController {
	
	@Autowired
	MenuService menuService;
	
	public Mono<ServerResponse> checkMenu(ServerRequest request) {
		log.info("check menu hit!!!");
		return ServerResponse.ok()
				.contentType(MediaType.TEXT_PLAIN)
				.body(Mono.just("check menu: " + System.currentTimeMillis()), String.class);
	}
	
	//add menu items
	public Mono<ServerResponse> addMenu(ServerRequest request) {
		Flux<FoodItem> items = request.bodyToFlux(FoodItem.class)
				.transformDeferred(FoodItemValidator::validateFoodItemMinimal);
		return items.collectList().flatMap(menuService::addMenuItems)
				.doOnError(e -> error(e))
				.flatMap(u -> okBody().bodyValue(u));
	}
	
	//update menu item
	public Mono<ServerResponse> updateFoodItem(ServerRequest request) {
		Mono<FoodItem> item = request.bodyToMono(FoodItem.class)
					.transformDeferred(FoodItemValidator::validateFoodItem);
		return item.flatMap(menuService::updateMenuItem)
				.doOnError(e -> error(e))
				.flatMap(r -> okBody().bodyValue(r));
	}
	
	//delete menu item
	public Mono<ServerResponse> deleteFoodItem(ServerRequest request) {
		Mono<FoodItem> item = request.bodyToMono(FoodItem.class)
						.transformDeferred(FoodItemValidator::validateFoodItem);
		return item.flatMap(menuService::deleteMenuItem)
				.doOnError(e -> error(e))
				.flatMap(o -> okBody().bodyValue("{\"message\" : \"item deleted.\"}"));
	}
	
	//read menu by restaurant
	public Mono<ServerResponse> getMenuByRestaurant(ServerRequest request) {
		Mono<String> restaurantId = Mono.just(request.pathVariable("rid"))
						.transformDeferred(HelperUtils::isNotNullOrEmpty);
		return restaurantId.flatMapMany(menuService::readMenuItems)
					.doOnError(e -> error(e))
				     .collectList()
					 .flatMap(resList -> okBody().bodyValue(resList));
	}

}
