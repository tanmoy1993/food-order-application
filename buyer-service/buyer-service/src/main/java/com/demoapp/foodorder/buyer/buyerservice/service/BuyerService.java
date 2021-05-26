package com.demoapp.foodorder.buyer.buyerservice.service;

import java.util.Optional;
import java.util.UUID;

import javax.validation.Valid;

import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.demoapp.foodorder.buyer.buyerservice.misc.BuyerServiceException;
import com.demoapp.foodorder.buyer.buyerservice.model.Buyer;
import com.demoapp.foodorder.buyer.buyerservice.model.PhoneNumber;
import com.demoapp.foodorder.buyer.buyerservice.model.PreferredContact;
import com.demoapp.foodorder.buyer.buyerservice.repository.AddressRepo;
import com.demoapp.foodorder.buyer.buyerservice.repository.BuyerRepo;
import com.demoapp.foodorder.buyer.buyerservice.repository.PhoneRepo;

@Service
@Transactional
public class BuyerService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(BuyerService.class);
	
	@Autowired
	BuyerRepo buyerRepo;

	@Autowired
	PhoneRepo phoneRepo;
	
	@Autowired
	AddressRepo addrRepo;
	
	public Iterable<Buyer> getAllBuyer() {
		return buyerRepo.findAll();
	}

	public Buyer getBuyerbyId(UUID id) {
		Buyer buyer = null;
		Optional<Buyer> buyerOpt = buyerRepo.findById(id);
		if(buyerOpt.isPresent())
			buyer =  buyerOpt.get();
		else
			throw new BuyerServiceException("Buyer id is invalid.");
		Hibernate.initialize(buyer.getAddresses());
		Hibernate.initialize(buyer.getPhones());
		return buyer;
	}
	
	public Buyer getBuyerbyIdLazy(UUID id) {
		Buyer buyer = null;
		Optional<Buyer> buyerOpt = buyerRepo.findById(id);
		if(buyerOpt.isPresent())
			buyer =  buyerOpt.get();
		else
			throw new BuyerServiceException("Buyer id is invalid.");
		return buyer;
	}
	
	public Buyer save(Buyer buyer) {
		if (userExistenceCheck(buyer.getFirstname(), buyer.getLastname()) != null)
			throw new BuyerServiceException("Buyer already exists.");
		String phones = "";
		for(PhoneNumber phno:buyer.getPhones())
			phones += phno.getPhoneNo() + ',';
		if(phoneRepo.existsPhoneNos(phones).equalsIgnoreCase("true"))
			throw new BuyerServiceException("Duplicate phone number provided.");
		return buyerRepo.save(buyer);
	}

	public Buyer userExistenceCheck(String firstname, String lastname) {
		return buyerRepo.findByFirstnameAndLastname(firstname, lastname);
	}

	public Buyer updateBuyer(Buyer buyer) {
		getBuyerbyIdLazy(buyer.getBuyerId());
		return buyerRepo.save(buyer);
	}

	public void deleteBuyer(UUID id) {
		getBuyerbyIdLazy(id);
		buyerRepo.deleteById(id);
	}

	public void updateBuyerPrefAddress(UUID buyerId, UUID tId) {
		Buyer buyer = getBuyerbyId(buyerId);
		int index = -1;
		for(int i=0; i < buyer.getAddresses().size(); i++) {
			if(buyer.getAddresses().get(i).getId().equals(tId)) {
				index = i;
				break;
			}
		}
		if(index == -1)
			throw new BuyerServiceException("Invalid address ID provided.");
		buyer.setPrefAddrIndex(index);
		buyerRepo.save(buyer);
		return;		
	}

	public void updateBuyerPrefPhone(@Valid UUID buyerId, @Valid UUID tId) {
		Buyer buyer = getBuyerbyId(buyerId);
		int index = -1;
		for(int i=0; i < buyer.getPhones().size(); i++) {
			if(buyer.getPhones().get(i).getId().equals(tId)) {
				index = i;
				break;
			}
		}
		if(index == -1)
			throw new BuyerServiceException("Invalid phone ID provided.");
		buyer.setPrefPhoneIndex(index);
		buyerRepo.save(buyer);
		return;		
	}
	
	public PreferredContact getPreferredContactPreferredContact(PreferredContact pc) {
		Buyer buyer = getBuyerbyId(pc.getBuyerId());
		LOGGER.info("buyer fetched: {}", buyer.toString());
		pc.setAddress(buyer.getAddresses().get(buyer.getPrefAddrIndex()));
		pc.setPhone(buyer.getPhones().get(buyer.getPrefPhoneIndex()));
		buyer.setOrderId(pc.getOrderId());
		buyerRepo.save(buyer);
		return pc;
	}

}
