package com.demoapp.foodorder.restaurant.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.demoapp.foodorder.restaurant.misc.HelperUtils;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Document(value = "FO_RESTAURANT_USER")
@ToString
@NoArgsConstructor
public class User {
	
	@Id
	@Getter @Setter
	private String id;
	
	@Field(value = "USER_ID")
	@Indexed(unique=true)
	private String userId;
	
	@Field(value = "USER_NAME")
	@Getter
	private String userName;
	
	@Field(value = "EMAIL")
	@Getter @Setter
	private String email;
	
	@Field(value = "PHONE")
	@Getter @Setter
	private String phone;
	
	@Field(value = "PASSWORD")
	@Getter
	private String passwordHash;

	public User(String userName, String email, String phone, String passwordHash) {
		this.userName = userName;
		this.email = email;
		this.phone = phone;
		this.passwordHash = passwordHash;
		this.userId = HelperUtils.generateHash(userName);
	}
	
	public void setUserName(String uname) {
		this.userName = uname;
		this.userId = HelperUtils.generateHash(uname);
	}
	
	public String getUserId() {
		if(this.userId==null)
			this.userId = HelperUtils.generateHash(this.userName);
		return this.userId;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

}
