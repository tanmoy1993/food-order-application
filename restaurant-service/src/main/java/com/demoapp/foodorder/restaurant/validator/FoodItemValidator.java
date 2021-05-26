package com.demoapp.foodorder.restaurant.validator;

import com.demoapp.foodorder.restaurant.misc.HelperUtils;
import com.demoapp.foodorder.restaurant.misc.ServerException;
import com.demoapp.foodorder.restaurant.model.FoodItem;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

public class FoodItemValidator {
	
	private static Mono<Tuple2<FoodItem, StringBuilder>> validateFoodItemCommon(Mono<FoodItem> foodItem) {
		Mono<StringBuilder> message = Mono.just(new StringBuilder());
		return foodItem.zipWith(message)
				.doOnNext(t -> {
					if(HelperUtils.isNullOrEmpty(t.getT1().getRestaurantId()))
						t.getT2().append("Restaurant id can not be null or empty.");
				})
				.doOnNext(t -> {
					if(t.getT1().getPrice() <= 0)
						t.getT2().append("Invalid price.");
				})
				.doOnNext(t -> {
					if(HelperUtils.isNullOrEmpty(t.getT1().getName()))
						t.getT2().append("Item name can not be null or empty.");
				});
	}
	
	public static Flux<FoodItem> validateFoodItemMinimal(Flux<FoodItem> foodItems) {
		return foodItems.flatMap(f ->
				Mono.just(f).transform(FoodItemValidator::validateFoodItemCommon)
						 .doOnNext(t -> t.getT1().setId(null))
						 .transform(HelperUtils::convertValidation))
				.onErrorMap(e -> new ServerException(e.getMessage()));
	}
	
	public static Mono<FoodItem> validateFoodItem(Mono<FoodItem> foodItem) {
		return foodItem.transform(FoodItemValidator::validateFoodItemCommon)
				.doOnNext(t -> {
					if(HelperUtils.isNullOrEmpty(t.getT1().getId()))
						t.getT2().append("FoodItem id can not be null or empty.");
				})
				.transform(HelperUtils::convertValidation);
	}

}
