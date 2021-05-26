package com.demoapp.foodorder.restaurant.misc;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.function.server.ServerResponse.BodyBuilder;

import com.demoapp.foodorder.restaurant.model.ExceptionResponse;

import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

public class HelperUtils {
	
	public static String generateHash(String input) {
		return DigestUtils.sha256Hex(input).toUpperCase();
	}
	
	public static BodyBuilder okBody() {
		return ServerResponse.ok()
				.contentType(MediaType.APPLICATION_JSON);
	}
	
	public static Mono<ServerResponse> error(Throwable e){
		return ServerResponse.badRequest()
					.contentType(MediaType.APPLICATION_JSON)
					.body(ExceptionResponse.buildExceptionResponse(e), ExceptionResponse.class);
	}
	
	public static boolean isNotNullOrEmpty(String s) {
		return s!=null && !s.isEmpty();
	}
	
	public static boolean isNullOrEmpty(String s) {
		return s==null || s.isEmpty();
	}
	
	public static Mono<String> isNotNullOrEmpty(Mono<String> str) {
		return str.filter(s -> s!=null && !s.isEmpty())
				.switchIfEmpty(Mono.error(new ServerException("Invalid argument.")));
	}
	
	public static <T> Mono<T> convertValidation(Mono<Tuple2<T, StringBuilder>> obj){
		return obj.map(t -> {
						if (t.getT2().toString().isEmpty())
							return t.getT1();
						else
							throw new ServerException(t.getT2().toString());
					}).onErrorMap(e -> new ServerException(e.getMessage()));
	}

}
