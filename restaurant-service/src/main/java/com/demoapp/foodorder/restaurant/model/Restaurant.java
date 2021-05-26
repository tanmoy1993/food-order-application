package com.demoapp.foodorder.restaurant.model;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Document(value = "FO_RESTAURANT_RESTAURANT")
@ToString
@NoArgsConstructor
public class Restaurant {
	
	@Id
	@Getter @Setter
	private String id;
	
	@Indexed
	@Field(value = "USER_DB_ID")
	@Getter @Setter
	private String userId;
	
	@Field(value = "EMAIL")
	@Getter @Setter
	private String email;
	
	@Field(value = "PHONE")
	@Getter @Setter
	private String phone;
	
	@Field(value = "RES_NAME")
	@Getter @Setter
	private String restaurantName;
	
	@Field(value = "PRICE_MIN")
	@Getter @Setter
	private int priceRangeMin;
	
	@Field(value = "PRICE_MAX")
	@Getter @Setter
	private int priceRangeMax;
	
	@Field(value = "ZIP_DELIVERED")
	@Getter @Setter
	private Set<Integer> zipCodesDelivered;

	public Restaurant(String userId, String email, String phone, String restaurantName, int priceRangeMin,
			int priceRangeMax) {

		this.userId = userId;
		this.email = email;
		this.phone = phone;
		this.restaurantName = restaurantName;
		this.priceRangeMin = priceRangeMin;
		this.priceRangeMax = priceRangeMax;
		
		this.zipCodesDelivered = new HashSet<>();
	}	

	public boolean addZipcode(int zip) {
		if(zipCodesDelivered.contains(zip))
			return false;
		else
			zipCodesDelivered.add(zip);
		return true;
	}
	
	public boolean removeZipcode(int zip) {
		if(zipCodesDelivered.contains(zip))
			return false;
		else
			zipCodesDelivered.remove(zip);
		return true;
	}
	
	public void updateUserDetail(User u) {
		this.email = u.getEmail();
		this.phone = u.getPhone();
	}

	public Restaurant(String userId, String restaurantName, int priceRangeMin, int priceRangeMax) {
		this.userId = userId;
		this.restaurantName = restaurantName;
		this.priceRangeMin = priceRangeMin;
		this.priceRangeMax = priceRangeMax;
		
		this.zipCodesDelivered = new HashSet<>();
	}
	
}
