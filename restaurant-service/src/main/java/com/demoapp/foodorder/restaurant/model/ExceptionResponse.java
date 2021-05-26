package com.demoapp.foodorder.restaurant.model;

import reactor.core.publisher.Mono;

public class ExceptionResponse {
	
	public String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ExceptionResponse() {
    }

    public ExceptionResponse(String message) {
        this.message = message;
    }

    public static Mono<ExceptionResponse> buildExceptionResponse(Exception ex){
        return Mono.just(new ExceptionResponse(ex.getMessage()));
    }
    
    public static Mono<ExceptionResponse> buildExceptionResponse(Throwable ex){
        return Mono.just(new ExceptionResponse(ex.getMessage()));
    }

}
