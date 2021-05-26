package com.demoapp.foodorder.buyer.buyerservice.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.validation.constraints.Pattern;

@Entity
@Table(name = "FO_BUYER_PHONE")
public class PhoneNumber {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PHONE_ID")
	private UUID id;

    @Column(name = "PHONE_NUMBER")
    @Pattern(regexp = "(\\+49)\\d{11}", message = "Invalid phone number.")
	private String phoneNo;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public PhoneNumber() {
	}

	public String getPhoneNo() {
		return phoneNo;
	}

	public void setPhoneNo(String phoneNo) {
		this.phoneNo = phoneNo;
	}

	@Override
	public String toString() {
		return "PhoneNumber [id=" + id + ", phoneNo=" + phoneNo + "]";
	}	
	
}
