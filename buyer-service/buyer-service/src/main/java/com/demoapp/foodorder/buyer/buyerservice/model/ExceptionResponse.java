package com.demoapp.foodorder.buyer.buyerservice.model;

public class ExceptionResponse {

    private String message;

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

}