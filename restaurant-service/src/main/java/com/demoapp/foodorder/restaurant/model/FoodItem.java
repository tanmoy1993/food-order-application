package com.demoapp.foodorder.restaurant.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Document(value = "FO_RESTAURANT_FOOD")
@ToString
@NoArgsConstructor
public class FoodItem {

	@Id
	@Getter @Setter
	private String id;
	
	@Indexed
	@Field(value = "RES_ID")
	@Getter @Setter
	private String restaurantId;
	
	@Field(value = "PRICE")
	@Getter @Setter
	private int price;
	
	@Field(value = "NAME")
	@Getter @Setter
	private String name;
	
	@Transient
	@Getter @Setter
	private int qty = 1;

	public FoodItem(String restaurantId, int price, String name) {
		this.restaurantId = restaurantId;
		this.price = price;
		this.name = name;
		this.qty = 1;
	}
		
}
