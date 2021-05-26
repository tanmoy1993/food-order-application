package com.demoapp.foodorder.buyer.buyerservice.repository;

import java.util.UUID;

import com.demoapp.foodorder.buyer.buyerservice.model.Address;

import org.springframework.data.repository.CrudRepository;

public interface AddressRepo extends CrudRepository<Address, UUID> {

	
}