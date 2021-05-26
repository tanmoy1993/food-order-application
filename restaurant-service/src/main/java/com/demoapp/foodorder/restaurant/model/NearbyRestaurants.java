package com.demoapp.foodorder.restaurant.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Document(value = "FO_RESTAURANT_NEARBY")
@ToString
@NoArgsConstructor
public class NearbyRestaurants {
	
	@Id
	@Getter
	private String id;
	
	@Indexed
	@Field(value = "ZIP")
	@Getter @Setter
	private int zip;
	
	@Field(value = "RES_ID")
	@Getter @Setter
	private String restaurantId;
	
	@Field(value = "RES_NAME")
	@Getter @Setter
	private String restaurantName;
	
	@Field(value = "PRICE_MIN")
	@Getter @Setter
	private int priceRangeMin;
	
	@Field(value = "PRICE_MAX")
	@Getter @Setter
	private int priceRangeMax;

	public NearbyRestaurants(int zip, String restaurantId, String restaurantName, int priceRangeMin, int priceRangeMax) {
		this.zip = zip;
		this.restaurantId = restaurantId;
		this.restaurantName = restaurantName;
		this.priceRangeMin = priceRangeMin;
		this.priceRangeMax = priceRangeMax;
	}
	
	public NearbyRestaurants(int zip, Restaurant r) {
		this.zip = zip;
		this.restaurantId = r.getId();
		this.restaurantName = r.getRestaurantName();
		this.priceRangeMin = r.getPriceRangeMin();
		this.priceRangeMax = r.getPriceRangeMax();
	}
	
	public void updateDetails(Restaurant r) {
		this.restaurantName = r.getRestaurantName();
		this.priceRangeMin = r.getPriceRangeMin();
		this.priceRangeMax = r.getPriceRangeMax();
	}

}
