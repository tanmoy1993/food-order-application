package com.demoapp.foodorder.buyer.buyerservice.model;

import java.util.UUID;

public class PreferredContact {
	
	private UUID buyerId;

	private String orderId;
	
	private PhoneNumber phone;
	
	private Address address;

	public PreferredContact(String orderId, UUID buyerId, PhoneNumber phone, Address address) {
		this.orderId = orderId;
		this.buyerId = buyerId;
		this.phone = phone;
		this.address = address;
	}

	public PreferredContact() {
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public PhoneNumber getPhone() {
		return phone;
	}

	public void setPhone(PhoneNumber phone) {
		this.phone = phone;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public UUID getBuyerId() {
		return buyerId;
	}

	public void setBuyerId(UUID buyerId) {
		this.buyerId = buyerId;
	}

	@Override
	public String toString() {
		return "PreferredContact [buyerId=" + buyerId + ", phone=" + phone + ", address=" + address + "]";
	}

}
