package com.demoapp.foodorder.restaurant.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class OrderCancelledTopic {
	
	@Getter @Setter
	private String orderId;
	
	@Getter @Setter
	private String restaurantId;
	
	@Getter @Setter
	private String reason;

}
