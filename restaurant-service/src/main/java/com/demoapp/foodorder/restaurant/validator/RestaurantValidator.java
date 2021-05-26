package com.demoapp.foodorder.restaurant.validator;

import com.demoapp.foodorder.restaurant.misc.HelperUtils;
import com.demoapp.foodorder.restaurant.model.Restaurant;

import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

public class RestaurantValidator {
	
	private static Mono<Tuple2<Restaurant, StringBuilder>> validateRestaurantCommon(Mono<Restaurant> restaurant) {
		Mono<StringBuilder> message = Mono.just(new StringBuilder());
		return restaurant.zipWith(message)
				.doOnNext(t -> {
					if(HelperUtils.isNullOrEmpty(t.getT1().getRestaurantName()))
						t.getT2().append("Restaurant name can not be null or empty.");
				})
				.doOnNext(t -> {
					if(HelperUtils.isNullOrEmpty(t.getT1().getUserId()))
						t.getT2().append("User id can not be null or empty.");
				})
				.doOnNext(t -> {
					if(t.getT1().getPriceRangeMin() <= 0 || t.getT1().getPriceRangeMin() >= t.getT1().getPriceRangeMax())
						t.getT2().append("Invalid price range.");
				})
				.doOnNext(t -> {
					if(t.getT1().getZipCodesDelivered()==null || t.getT1().getZipCodesDelivered().size()==0)
						t.getT2().append("At least one zip code needed.");
				});
	}
	
	public static Mono<Restaurant> validateRestaurantMinimal(Mono<Restaurant> restaurant) {
		return restaurant.transform(RestaurantValidator::validateRestaurantCommon)
						 .doOnNext(t -> t.getT1().setId(null))
						 .transform(HelperUtils::convertValidation);
	}
	
	public static Mono<Restaurant> validateRestaurant(Mono<Restaurant> restaurant) {
		return restaurant.transform(RestaurantValidator::validateRestaurantCommon)
				.doOnNext(t -> {
					if(HelperUtils.isNullOrEmpty(t.getT1().getId()))
						t.getT2().append("Restaurant id can not be null or empty.");
				}).transform(HelperUtils::convertValidation);
	}

}
