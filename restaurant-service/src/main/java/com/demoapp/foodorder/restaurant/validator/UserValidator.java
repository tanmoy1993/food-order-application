package com.demoapp.foodorder.restaurant.validator;

import com.demoapp.foodorder.restaurant.misc.HelperUtils;
import com.demoapp.foodorder.restaurant.model.User;

import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

public class UserValidator {

	private static Mono<Tuple2<User, StringBuilder>> validateUserCommon(Mono<User> user) {
		Mono<StringBuilder> message = Mono.just(new StringBuilder());
		return user.zipWith(message)
				.doOnNext(t -> {
					if (HelperUtils.isNullOrEmpty(t.getT1().getUserName()))
						t.getT2().append("User name can not be null or empty.");
				}).doOnNext(t -> {
					if (HelperUtils.isNullOrEmpty(t.getT1().getPasswordHash()))
						t.getT2().append("Password can not be null or empty.");
				});
	}

	public static Mono<User> validateUserMinimal(Mono<User> user) {
		return user.transform(UserValidator::validateUserCommon)
				.doOnNext(t -> t.getT1().setId(null))
				.transform(HelperUtils::convertValidation);
	}

	public static Mono<User> validateUser(Mono<User> user) {
		return user.transform(UserValidator::validateUserCommon)
				.doOnNext(t -> {
					if (HelperUtils.isNullOrEmpty(t.getT1().getPhone()) 
							&& HelperUtils.isNullOrEmpty(t.getT1().getEmail()))
						t.getT2().append("Either Phone or Email needed.");
				}).transform(HelperUtils::convertValidation);
	}

}
