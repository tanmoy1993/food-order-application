package com.demoapp.foodorder.restaurant.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.demoapp.foodorder.restaurant.model.FoodItem;
import com.demoapp.foodorder.restaurant.repository.FoodItemRepo;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class MenuService {
	
	@Autowired
	FoodItemRepo foodItemRepo;
	
	public Mono<List<FoodItem>> addMenuItems(List<FoodItem> foodItems){
		return foodItemRepo.saveAll(foodItems).collectList();
	}
	
	public Flux<FoodItem> readMenuItems(String restaurantId){
		return foodItemRepo.findByRestaurantId(restaurantId)
				.switchIfEmpty(Mono.error(new Exception("Restaurant does not exist!!")));
	}
	
	public Mono<Void> deleteMenuItem(FoodItem foodItem){
		return foodItemRepo.delete(foodItem);
	}
	
	public Mono<FoodItem> updateMenuItem(FoodItem foodItem){
		return foodItemRepo.findById(foodItem.getId())
				.switchIfEmpty(Mono.error(new Exception("Invalid menu item.")))
				.map(f -> {foodItemRepo.save(foodItem).subscribe(); return foodItem;});
	}
	
	private int getPrice(Map<String, FoodItem> t1, Map<String, FoodItem> t2) {
		int total=0;
		for(String id:t1.keySet()) {
			total += t1.get(id).getQty() * t2.get(id).getPrice();
		}
		log.info("calculated total: {}", total);
		return total;
	}
	
	public Mono<Boolean> verifyPrice(List<FoodItem> foodItems, int expectedPrice) {
		Mono<Map<String, FoodItem>> foodItemMap = Flux.fromIterable(foodItems).collectMap(f -> f.getId()).cache();
		Mono<Map<String, FoodItem>> foodItemDbMap = Flux.fromIterable(foodItems).map(FoodItem::getId)
				.collectList().flatMapMany(fil -> foodItemRepo.findAllById(fil))
							.collectMap(f -> f.getId()).cache();
		return foodItemMap.zipWith(foodItemDbMap, (t1, t2) -> getPrice(t1, t2))
								.map(total -> total == expectedPrice);
	}
	

}
