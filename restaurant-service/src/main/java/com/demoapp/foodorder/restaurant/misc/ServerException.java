package com.demoapp.foodorder.restaurant.misc;

public class ServerException extends IllegalArgumentException {

	private String msg;
	
	public ServerException(String msg) {
		this.msg = msg;
	}
	
	public String getMessage() {
		return msg;
	}

}
