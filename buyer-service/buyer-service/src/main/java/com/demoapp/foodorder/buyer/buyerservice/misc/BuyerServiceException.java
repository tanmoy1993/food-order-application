package com.demoapp.foodorder.buyer.buyerservice.misc;

public class BuyerServiceException extends IllegalArgumentException {
	
	private String exMessage;

	public BuyerServiceException(String exMessage) {
		this.exMessage = exMessage;
	}

	public void setMessage(String message) {
		this.exMessage = message;
	}
	
	@Override
	public String getMessage() {
		return this.exMessage;
	}

}
