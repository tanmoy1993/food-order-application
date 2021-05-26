package com.demoapp.foodorder.restaurant.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class OrderReceivedTopic {
	
	@Getter @Setter
	private int totalCost;
	
	@Getter @Setter
	private String orderId;
	
	@Getter @Setter
	private String restaurantId;
	
	@Getter @Setter
	private List<FoodItem> orderedItems;

}
