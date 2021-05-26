package com.demoapp.foodorder.buyer.buyerservice.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.demoapp.foodorder.buyer.buyerservice.model.PhoneNumber;

public interface PhoneRepo extends CrudRepository<PhoneNumber, UUID> {

	public PhoneNumber findByPhoneNo(String phoneNo);

	@Query(value = "SELECT EXISTS(SELECT 1 FROM FO_BUYER_PHONE WHERE PHONE_NUMBER IN (?1) limit 1)", nativeQuery = true) 
	public String existsPhoneNos(String phoneNos);
	
}