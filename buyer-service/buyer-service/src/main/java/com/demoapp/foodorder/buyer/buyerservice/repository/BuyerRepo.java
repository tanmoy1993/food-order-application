package com.demoapp.foodorder.buyer.buyerservice.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

import com.demoapp.foodorder.buyer.buyerservice.model.Buyer;

public interface BuyerRepo extends CrudRepository<Buyer, UUID> {

	Buyer findByFirstnameAndLastname(String firstname,String lastname);
	
	@Query(value = "UPDATE FO_BUYER SET P_AD_INDEX = ?1 WHERE BUYER_ID = ?2", nativeQuery = true)
	void updateAddressPreference(int preference, UUID buyerId);
	
	@Query(value = "UPDATE FO_BUYER SET P_PH_INDEX = ?1 WHERE BUYER_ID = ?2", nativeQuery = true)
	void updatePhonePreference(int preference, UUID buyerId);
	
}
