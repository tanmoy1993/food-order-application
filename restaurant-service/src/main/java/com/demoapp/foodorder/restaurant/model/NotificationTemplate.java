package com.demoapp.foodorder.restaurant.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class NotificationTemplate {
	
	@Getter @Setter
	private String contactMethod;
	
	@Getter @Setter
	private String contactDetails;
	
	@Getter @Setter
	private String subject;
	
	@Getter @Setter
	private List<FoodItem> orderedItems;


}
