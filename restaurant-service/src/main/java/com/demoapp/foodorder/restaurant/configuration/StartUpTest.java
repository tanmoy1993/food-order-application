package com.demoapp.foodorder.restaurant.configuration;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

import com.demoapp.foodorder.restaurant.model.FoodItem;
import com.demoapp.foodorder.restaurant.model.NearbyRestaurants;
import com.demoapp.foodorder.restaurant.model.Restaurant;
import com.demoapp.foodorder.restaurant.model.User;
import com.demoapp.foodorder.restaurant.repository.RestaurantRepo;
import com.demoapp.foodorder.restaurant.repository.UserRepo;
import com.demoapp.foodorder.restaurant.service.MenuService;
import com.demoapp.foodorder.restaurant.service.RestaurantService;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Configuration
@Slf4j
public class StartUpTest implements
 		ApplicationListener<ContextRefreshedEvent> {
	
	@Autowired
	UserRepo userRepo;
	
	@Autowired
	RestaurantRepo resRepo;
	
	@Autowired
	RestaurantService restaurantService;
	
	@Autowired
	MenuService menuService;

     @Override
     public void onApplicationEvent(ContextRefreshedEvent arg0) {
    	 
    	 log.info("--------------------------------------------------");
    	 log.info("--------------------------------------------------");
    	 log.info("--------------------------------------------------");
    	 log.info("--------------------------------------------------");
    	 log.info("--------------------------------------------------");
    	 log.info("--------------------------------------------------");
    	 try {
			run();
			log.info("-------------------SUCCESS---------------------");
		} catch (Exception e) {
			e.printStackTrace();
		}

     }
     
     private void sleep() {
    	 try {
    		 Thread.sleep(3000);
    	 } catch(Exception e) {
    		 
    	 }
     }
     
     private int get_rand_long(){
 		return (int)(Math.random() * 10000);
 	}
 	
 	private User randomUser() {
 		return new User("userName" + get_rand_long(), "email" + get_rand_long() + "@xx.com" , 
 				"16550" + get_rand_long(), "passwordHash" + get_rand_long());
 	}
 	
 	private User copyUser(User src) {
 		return new User(src.getUserName(), src.getEmail(), src.getPhone(), src.getPasswordHash()); 		
 	}
 	
 	private Restaurant randomRestaurant(String userId) {
 		return new Restaurant(userId, "restaurantName" + get_rand_long(), 300, 800);
 	}
 	
 	private Restaurant copyRestaurant(Restaurant src) {
 		Restaurant dest = new Restaurant(src.getUserId(), src.getEmail(), 
 				src.getPhone(), src.getRestaurantName(), src.getPriceRangeMin(), src.getPriceRangeMax());
 		for(int zip:src.getZipCodesDelivered())
 			dest.addZipcode(zip);
 		return dest;
 	}
 	
 	private FoodItem randomFI(String restId) {
 		return new FoodItem(restId, get_rand_long(), "foodName" + get_rand_long());
 	} 	
 	
 	private void assertEqual(String actual, String expected, String step) throws Exception {
 		if(!actual.equalsIgnoreCase(expected)) {
 			throw new Exception("step: " + step + ", expected: " + expected + ", actual: " + actual);
 		}
 	}
 	
 	private void assertNotNull(String actual, String step) throws Exception {
 		if(actual == null) {
 			throw new Exception("step(null check): " + step + ", actual: " + actual);
 		}
 	}
 	
 	private void assertTrue(boolean actual, String step) throws Exception {
 		if(!actual) {
 			throw new Exception("step failed: " + step);
 		}
 	}
 	
 	public void run() throws Exception {
 		
 		//create user 1
 		User user10 = randomUser();
 		String pass = user10.getPasswordHash();
 		User user1 = copyUser(user10);
 		user1 = restaurantService.createUser(Mono.just(user1)).block();
 		assertEqual(user1.getUserId(), user10.getUserId(), "user1 create");
 		assertEqual(user1.getPasswordHash(), "****", "user1 create");
 		assertNotNull(user1.getId(), "user1 create");
 		user1.setPasswordHash(pass);
 		user10.setPasswordHash(pass);
 		log.info("User 1: {}", user1);
 		
 		//create user 2
 		User user20 = randomUser();
 		pass = user20.getPasswordHash();
 		User user2 = copyUser(user20);
 		user2 =	restaurantService.createUser(Mono.just(user2)).block();
 		assertEqual(user2.getUserId(), user20.getUserId(), "user2 create");
 		assertEqual(user2.getPasswordHash(), "****", "user2 create");
 		assertNotNull(user2.getId(), "user2 create");
 		user2.setPasswordHash(pass);
 		user20.setPasswordHash(pass);
 		log.info("User 2: {}", user2);
 		
 		//read user 2 with correct password
 		User user21 = restaurantService.validateAndFetchUser(Mono.just(user20)).block();
 		assertNotNull(user21.getId(), "user2 verify");
 		
 		//read user 1 with wrong password
 		user10.setPasswordHash("passwordHash");
 		try{
 			restaurantService.validateAndFetchUser(Mono.just(user10))
 			.doOnError(u -> log.info("invalid user result error: {}", u))
 			.hasElement()
 			.block();
 			throw new Exception("FAIL: invalid user password case failed.");
 		} catch(Exception e) {
 			if(e.getMessage().startsWith("FAIL:"))
 				throw e;
 		}
 		
 		user10.setPasswordHash(user1.getPasswordHash());
 		user10.setUserName("uname");
 		try {
 			restaurantService.validateAndFetchUser(Mono.just(user10))
 				.doOnError(u -> log.info("invalid user result error: {}", u))
 				.hasElement()
 				.block();
 			throw new Exception("FAIL: invalid user uname case failed.");
 		} catch(Exception e) {
 			if(e.getMessage().startsWith("FAIL:"))
 				throw e;
 		}
 		
 		//read all user - skipping as not in service
 		
 		//repeated user name
		try {
			user20.setId(null);
			User obj = restaurantService.createUser(Mono.just(user20))
					.doOnError(u -> log.info("repeated user name result error: {}", u))
					.block();
			log.info("created user: {}", obj);
			throw new Exception("FAIL: repeated username case failed.");
		} catch (Exception e) {
			if (e.getMessage().startsWith("FAIL:"))
				throw e;
		}

 		//create rest 11 user 1 - zip 1,2
 		Restaurant rest110 = randomRestaurant(user1.getId());
 		rest110.addZipcode(45001);
 		rest110.addZipcode(45002);
 		Restaurant rest11 = copyRestaurant(rest110);
 		rest11 = restaurantService.createRestaurant(rest11).block();
 		assertNotNull(rest11.getId(), "rest 11 create");
 		assertEqual(rest11.getEmail(), user1.getEmail(), "rest 11 create");
 		
 		//create rest 12 user 1 - zip 2,3
 		Restaurant rest120 = randomRestaurant(user1.getId());
 		rest120.addZipcode(45003);
 		rest120.addZipcode(45002);
 		Restaurant rest12 = copyRestaurant(rest120);
 		rest12 = restaurantService.createRestaurant(rest12).block();
 		assertNotNull(rest12.getId(), "rest 12 create");
 		assertEqual(rest12.getPhone(), user1.getPhone(), "rest 12 create");
 		
 		//create rest 21 user 2 - zip 1,3
 		Restaurant rest210 = randomRestaurant(user1.getId());
 		rest210.addZipcode(45001);
 		rest210.addZipcode(45003);
 		Restaurant rest21 = copyRestaurant(rest210);
 		rest21 = restaurantService.createRestaurant(rest21).block();
 		assertNotNull(rest21.getId(), "rest 21 create");
 		
 		sleep();
 		
 		//near by rest - zip 1 - rest 11,21
 		List<NearbyRestaurants> nearZip1 = restaurantService.fetchRestaurantsZip(45001).collectList().block();
 		String nearZip1S = nearZip1.toString();
 		log.info("nearZip1S: {}", nearZip1S);
 		if(!(nearZip1S.contains(rest11.getId()) && 
 				nearZip1S.contains(rest21.getId()) && !nearZip1S.contains(rest12.getId())))
 			throw new Exception("near by rest - zip 1 - rest 11,21 failed");
 		
 		//near by rest - zip 2 - rest 11,12
 		List<NearbyRestaurants> nearZip2 = restaurantService.fetchRestaurantsZip(45002).collectList().block();
 		String nearZip2S = nearZip2.toString();
 		log.info("nearZip2S: {}", nearZip2S);
 		if(!(nearZip2S.contains(rest11.getId()) && 
 				nearZip2S.contains(rest12.getId()) && !nearZip2S.contains(rest21.getId())))
 			throw new Exception("near by rest - zip 2 - rest 11,12 failed");
 		
 		//update rest 11 - zip 3
 		rest11.addZipcode(45003);
 		Restaurant restaurant11u = restaurantService.updateRestaurant(rest11).block();
 		assertTrue(restaurant11u.toString().contains("45003"), "update rest 11 - zip 3");
 		
 		sleep();
 		
 		//near by rest - zip 3 - rest 11,12,21
 		List<NearbyRestaurants> nearZip3 = restaurantService.fetchRestaurantsZip(45003).collectList().block();
 		String nearZip3S = nearZip3.toString();
 		log.info("nearZip3S: {}", nearZip3S);
 		if(!(nearZip3S.contains(rest11.getId()) && 
 				nearZip3S.contains(rest12.getId()) && nearZip3S.contains(rest21.getId())))
 			throw new Exception("near by rest - zip 3 - rest 11,12,21 failed");
 		
 		//add food item 11,12 rest 11
 		FoodItem fi110 = randomFI(rest11.getId());
 		FoodItem fi120 = randomFI(rest11.getId());
 		List<FoodItem> fi10 = new ArrayList<>();
 		fi10.add(fi110);
 		fi10.add(fi120);
 		List<FoodItem> fi1 = menuService.addMenuItems(fi10).block();
 		assertTrue(fi1.toString().contains(fi110.getName()) 
 				&& fi1.toString().contains(fi120.getName()), "add food item 11,12 rest 11");
 		assertNotNull(fi1.get(0).getId(), "add food item 11,12 rest 11 - id 0");
 		assertNotNull(fi1.get(1).getId(), "add food item 11,12 rest 11 - id 0");
 		
 		//add food item 21,22 rest 21
 		FoodItem fi210 = randomFI(rest21.getId());
 		FoodItem fi220 = randomFI(rest21.getId());
 		List<FoodItem> fi20 = new ArrayList<>();
 		fi20.add(fi210);
 		fi20.add(fi220);
 		List<FoodItem> fi2 = menuService.addMenuItems(fi20).block();
 		assertTrue(fi2.toString().contains(fi210.getName()) 
 				&& fi2.toString().contains(fi220.getName()), "add food item 21,22 rest 21");
 		assertNotNull(fi2.get(0).getId(), "add food item 21,22 rest 21 - id 0");
 		assertNotNull(fi2.get(1).getId(), "add food item 21,22 rest 21 - id 0");
 		
 		//add food item 23 rest 21
 		FoodItem fi230 = randomFI(rest21.getId());
 		List<FoodItem> fi230l = new ArrayList<>();
 		fi230l.add(fi230);
 		List<FoodItem> fi23l = menuService.addMenuItems(fi230l).block();
 		assertTrue(fi23l.toString().contains(fi230.getName()), "add food item 23 rest 21");
 		assertNotNull(fi23l.get(0).getId(), "add food item 23 rest 21 - id 0");
 		
 		sleep();
 		
 		//read food item by rest 21 - fi 21,22,23
 		List<FoodItem> fiRest21 = menuService.readMenuItems(rest21.getId()).collectList().block();
 		assertTrue(fiRest21.toString().contains(fi210.getName()), "read food item by rest 21 - fi 21");
 		assertTrue(fiRest21.toString().contains(fi220.getName()), "read food item by rest 21 - fi 22");
 		assertTrue(fiRest21.toString().contains(fi230.getName()), "read food item by rest 21 - fi 23");
 		assertTrue(fiRest21.size()==3, "read food item by rest 21");
 		
 		//update food item 12
 		FoodItem fi120u = fi10.get(1);
 		fi120u.setName("120u-name");
 		FoodItem fi12u = menuService.updateMenuItem(fi120u).block();
 		assertEqual(fi12u.getId(), fi120u.getId(), "update food item 12");
 		
 		sleep();
 		
 		//read food item by rest 11 - fi 11,12u
 		List<FoodItem> fiRest11 = menuService.readMenuItems(rest11.getId()).collectList().block();
 		assertTrue(fiRest11.toString().contains(fi110.getName()), "read food item by rest 21 - fi 11");
 		assertTrue(fiRest11.toString().contains(fi120u.getName()), "read food item by rest 21 - fi 12");
 		assertTrue(fiRest11.size()==2, "read food item by rest 11");
 		
 		//fi 21,23 -> price
 		int expectedPrice = fi210.getPrice() + fi220.getPrice();
 		Boolean priceCheck = menuService.verifyPrice(fi2, expectedPrice).block();
 		log.info("calculated price: {}", priceCheck);
 		assertTrue(priceCheck, "fi 21,23 -> price");
 		
 		//delete fi 22
 		menuService.deleteMenuItem(fi20.get(1)).block();
 		
 		sleep();
 		
 		//read food item by rest 21 - fi 21,23
 		fiRest21 = menuService.readMenuItems(rest21.getId()).collectList().block();
 		assertTrue(fiRest21.toString().contains(fi210.getName()), "read food item by rest 21 u - fi 21");
 		assertTrue(!fiRest21.toString().contains(fi220.getName()), "read food item by rest 21 u - fi 22");
 		assertTrue(fiRest21.toString().contains(fi230.getName()), "read food item by rest 21 u - fi 23");
 		assertTrue(fiRest21.size()==2, "read food item by rest 21 u");
 		
 		//delete restaurant 11
 		restaurantService.deleteRestaurant(rest11).block();
 		
 		sleep();
 		
 		//read food item by rest 11 - exception
 		try {
 			menuService.readMenuItems(rest11.getId()).collectList().block();
 			throw new Exception("read food item by rest 11 - exception");
 		}catch(Exception e) {
 			log.info("read food item by rest 11 - exception: {}", e.getMessage());
 			if(!e.getMessage().contains("Restaurant does not exist!!"))
 				throw e;
 		}
 		
 		//near by rest - zip 1 - rest 21
 		List<NearbyRestaurants> nearZip4 = restaurantService.fetchRestaurantsZip(45001).collectList().block();
 		String nearZip4S = nearZip4.toString();
 		log.info("nearZip4S: {}", nearZip4S);
 		if(!(nearZip4S.contains(rest21.getId()) && 
 				!nearZip4S.contains(rest12.getId()) && !nearZip4S.contains(rest11.getId())))
 			throw new Exception("near by rest - zip 1 - rest 21 failed");
 		
 		//near by rest - zip 3 - rest 12,21
 		nearZip4 = restaurantService.fetchRestaurantsZip(45003).collectList().block();
 		nearZip4S = nearZip4.toString();
 		log.info("nearZip4S: {}", nearZip4S);
 		if(!(nearZip4S.contains(rest21.getId()) && 
 				nearZip4S.contains(rest12.getId()) && !nearZip4S.contains(rest11.getId())))
 			throw new Exception("near by rest - zip 1 - rest 21 failed");
 		
 		//update invalid restaurant
 		try {
 			restaurantService.updateRestaurant(rest11)
 				.doOnSuccess(x -> Mono.error(new Exception("update invalid restaurant - exception"))).block();
 		}catch(Exception e) {
 			log.info("update invalid restaurant - exception: {}", e.getMessage());
 			if(!e.getMessage().contains("Restaurant does not exist."))
 				throw e;
 		}
 		
 		//update invalid food item
 		try {
 			menuService.updateMenuItem(fi20.get(1))
 				.doOnSuccess(x -> Mono.error(new Exception("update invalid food item - exception"))).block();
 		}catch(Exception e) {
 			log.info("update invalid food item - exception: {}", e.getMessage());
 			if(!e.getMessage().contains("Invalid menu item."))
 				throw e;
 		}
 		
 	}
}