package com.demoapp.foodorder.restaurant.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.demoapp.foodorder.restaurant.controller.MenuController;
import com.demoapp.foodorder.restaurant.controller.RestaurantController;

@Configuration
public class WebConf {

	/*
	 * @Bean public MethodValidationPostProcessor methodValidationPostProcessor() {
	 * return new MethodValidationPostProcessor(); }
	 * 
	 */
	
	private RequestPredicate acceptPredicate() {
		return RequestPredicates.accept(MediaType.APPLICATION_JSON);
	}

	@Bean
	public RouterFunction<ServerResponse> routeRestaurant(RestaurantController restaurantController) {

		return RouterFunctions.route().GET("/check", restaurantController::check)
				.GET("/user/{uid}/restaurant", acceptPredicate(), restaurantController::getRestaurantByUser)
				.GET("/zip/{zip}/restaurant", acceptPredicate(), restaurantController::getRestaurantByZip)
				.POST("/user", acceptPredicate(), restaurantController::createUser)
				.POST("/login", acceptPredicate(), restaurantController::userLogin)
				.POST("/restaurant", acceptPredicate(), restaurantController::createRestaurant)
				.PUT("/restaurant", acceptPredicate(), restaurantController::updateRestaurant)
				.DELETE("/restaurant", acceptPredicate(), restaurantController::deleteRestaurant)
				.onError(e -> e instanceof Exception, RestaurantController::exceptionProcessor)
				.build();
		
	}
	
	@Bean
	public RouterFunction<ServerResponse> routeMenu(MenuController menuController) {

		return RouterFunctions.route().GET("/checkmenu", menuController::checkMenu)
				.GET("/restaurant/{rid}/food", acceptPredicate(), menuController::getMenuByRestaurant)
				.POST("/food", acceptPredicate(), menuController::addMenu)
				.PUT("/food", acceptPredicate(), menuController::updateFoodItem)
				.DELETE("/food", acceptPredicate(), menuController::deleteFoodItem)
				.onError(e -> e instanceof Exception, RestaurantController::exceptionProcessor)
				.build();
		
	}

}