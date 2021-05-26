package com.demoapp.foodorder.buyer.buyerservice.configuration;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.Transactional;

import com.demoapp.foodorder.buyer.buyerservice.model.Address;
import com.demoapp.foodorder.buyer.buyerservice.model.Buyer;
import com.demoapp.foodorder.buyer.buyerservice.model.PhoneNumber;
import com.demoapp.foodorder.buyer.buyerservice.repository.BuyerRepo;
import com.demoapp.foodorder.buyer.buyerservice.service.BuyerService;

@SpringBootApplication
@ComponentScan(basePackages={"com.demoapp.foodorder.buyer"})
public class BuyerServiceApplication implements CommandLineRunner {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(BuyerServiceApplication.class);
	
	@Autowired
	BuyerRepo buyerRepo;

	public static void main(String[] args) {
		SpringApplication.run(BuyerServiceApplication.class, args);
	}

	private int get_rand_long(){
		return (int)(Math.random() * 10000);
	}

	@Override
	@Transactional
	public void run(String... arg0) throws Exception {

		PhoneNumber bp11 = new PhoneNumber();
		bp11.setPhoneNo("+49" + (12345670000L + get_rand_long()));
		Address ba11 = new Address();
		ba11.setAddressLineNo1("addressLineNo1" + get_rand_long());
		ba11.setZipCode(50000 + get_rand_long());
		Address ba12 = new Address();
		ba12.setAddressLineNo1("addressLineNo1" + get_rand_long());
		ba12.setZipCode(50000 + get_rand_long());
		Buyer b1 = new Buyer();
		b1.setFirstname("FB name " + get_rand_long());
		b1.setLastname("LB name " + get_rand_long());
		List<PhoneNumber> bpl1 = new ArrayList<>();
		bpl1.add(bp11);
		b1.setPhones(bpl1);
		List<Address> bal1 = new ArrayList<>();
		bal1.add(ba11);
		bal1.add(ba12);
		b1.setAddresses(bal1);
		
		PhoneNumber bp21 = new PhoneNumber();
		bp21.setPhoneNo("+49" + (12345670000L + get_rand_long()));
		PhoneNumber bp22 = new PhoneNumber();
		bp22.setPhoneNo("+49" + (12345670000L + get_rand_long()));
		Address ba21 = new Address();
		ba21.setAddressLineNo1("addressLineNo1" + get_rand_long());
		ba21.setZipCode(50000 + get_rand_long());
		Buyer b2 = new Buyer();
		b2.setFirstname("FB name " + get_rand_long());
		b2.setLastname("LB name " + get_rand_long());
		List<PhoneNumber> bpl2 = new ArrayList<>();
		bpl2.add(bp21);
		bpl2.add(bp22);
		b2.setPhones(bpl2);
		List<Address> bal2 = new ArrayList<>();
		bal2.add(ba21);
		b2.setAddresses(bal2);
		
		buyerRepo.save(b1);
		buyerRepo.save(b2);
		
		LOGGER.info("Fetched buyers: {}", buyerRepo.findAll());
		
	}

}
