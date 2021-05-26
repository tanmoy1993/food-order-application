package com.demoapp.foodorder.restaurant.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.demoapp.foodorder.restaurant.misc.ServerException;
import com.demoapp.foodorder.restaurant.model.NearbyRestaurants;
import com.demoapp.foodorder.restaurant.model.Restaurant;
import com.demoapp.foodorder.restaurant.model.User;
import com.demoapp.foodorder.restaurant.repository.FoodItemRepo;
import com.demoapp.foodorder.restaurant.repository.NearbyRestaurantRepo;
import com.demoapp.foodorder.restaurant.repository.RestaurantRepo;
import com.demoapp.foodorder.restaurant.repository.UserRepo;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@Slf4j
public class RestaurantService {

	@Autowired
	UserRepo userRepo;

	@Autowired
	RestaurantRepo restaurantRepo;

	@Autowired
	NearbyRestaurantRepo nearRestaurantRepo;

	@Autowired
	FoodItemRepo foodItemRepo;

	private class ZipEntry {
		public int zip;
		public String action;

		public ZipEntry(int zip, String action) {
			this.zip = zip;
			this.action = action;
		}
	}

	private Flux<ZipEntry> getModifiedZipEntries(Restaurant oldR, Restaurant newR) {
		Set<ZipEntry> zipEntries = new HashSet<>();
		for (Integer zip : oldR.getZipCodesDelivered()) {
			if (!newR.getZipCodesDelivered().contains(zip))
				zipEntries.add(new ZipEntry(zip, "remove"));
			else
				zipEntries.add(new ZipEntry(zip, "update"));
		}
		for (Integer zip : newR.getZipCodesDelivered()) {
			if (!oldR.getZipCodesDelivered().contains(zip))
				zipEntries.add(new ZipEntry(zip, "add"));
		}
		return Flux.fromIterable(zipEntries);
	}

	// create user
	public Mono<User> createUser(Mono<User> user) {
		return user.flatMap(userRepo::save).doOnError(e -> log.error("duplicate user name: {}", e.getMessage()))
				.onErrorMap(e -> !(e instanceof ServerException), e -> new ServerException("User name already exists."))
				.doOnNext(u -> u.setPasswordHash("****"))
				.switchIfEmpty(Mono.error(new ServerException("User name already exists.")));
	}

	// validate user
	public Mono<User> validateAndFetchUser(Mono<User> user) {
		return user.flatMap(u -> userRepo.findFirstByUserIdAndPasswordHash(u.getUserId(), u.getPasswordHash()))
				.doOnNext(u -> u.setPasswordHash("****"))
				.switchIfEmpty(Mono.error(new ServerException("User name and password does not exist.")));
	}
	
	// create restaurant for user
	// need NearbyUpdate
	public Mono<Restaurant> createRestaurant(Restaurant restaurant) {
		return userRepo.findById(restaurant.getUserId())
				.doOnNext(u -> restaurant.updateUserDetail(u))
				.switchIfEmpty(Mono.error(new ServerException("Invalid user Id.")))
				.flatMap(u -> restaurantRepo.save(restaurant)).doOnNext(r -> {
			List<NearbyRestaurants> nbRests = new ArrayList<>();
			r.getZipCodesDelivered().forEach(zip -> {
				nbRests.add(new NearbyRestaurants(zip, r));
			});
			nearRestaurantRepo.insert(nbRests).subscribeOn(Schedulers.boundedElastic()).collectList().subscribe();
		});
	}

	// update restaurant
	// need NearbyUpdate
	public Mono<Restaurant> updateRestaurant(Restaurant restaurant) {
		Mono<Restaurant> restDb = restaurantRepo.findById(restaurant.getId())
				.switchIfEmpty(Mono.error(new ServerException("Restaurant does not exist."))).cache();

		restDb.onErrorStop().flatMapMany(r -> getModifiedZipEntries(r, restaurant)).switchIfEmpty(Mono.empty())
				.subscribeOn(Schedulers.boundedElastic()).parallel().doOnNext(ze -> {
					log.info("updating for zip:{}, action: {}", ze.zip, ze.action);
					if (ze.action.equalsIgnoreCase("add"))
						nearRestaurantRepo.insert(new NearbyRestaurants(ze.zip, restaurant)).subscribe();
					else if (ze.action.equalsIgnoreCase("update"))
						nearRestaurantRepo.findByRestaurantIdAndZip(restaurant.getId(), ze.zip)
								.doOnNext(r -> r.updateDetails(restaurant)).flatMap(nearRestaurantRepo::save)
								.subscribe();
					else
						nearRestaurantRepo.findByRestaurantIdAndZip(restaurant.getId(), ze.zip)
								.flatMap(r -> nearRestaurantRepo.deleteById(r.getId())).subscribe();
				}).subscribe();
		
		return restDb.flatMap(r -> restaurantRepo.save(restaurant));
	}

	// read restaurant for user
	public Flux<Restaurant> fetchRestaurantsUser(String userId) {
		return restaurantRepo.findAllByUserId(userId)
				.switchIfEmpty(Mono.error(new ServerException("User does not exist.")));
	}
	
	public Mono<Restaurant> fetchRestaurant(String restId) {
		return restaurantRepo.findById(restId)
				.switchIfEmpty(Mono.error(new ServerException("Restaurant does not exist.")));
	}

	// delete restaurant
	// need NearbyUpdate
	// delete food items
	public Mono<Void> deleteRestaurant(Restaurant restaurant) {
		restaurantRepo.findById(restaurant.getId()).flatMapMany(r -> Flux.fromIterable(r.getZipCodesDelivered()))
				.switchIfEmpty(Mono.empty()).subscribeOn(Schedulers.boundedElastic()).parallel().doOnNext(zip -> {
					nearRestaurantRepo.findByRestaurantIdAndZip(restaurant.getId(), zip)
							.doOnNext(r -> nearRestaurantRepo.deleteById(r.getId()).subscribe()).subscribe();
				}).subscribe();
		foodItemRepo.deleteByRestaurantId(restaurant.getId()).subscribe();
		return restaurantRepo.deleteById(restaurant.getId());
	}

	// read nearby restaurants for zip
	public Flux<NearbyRestaurants> fetchRestaurantsZip(int zip) {
		return nearRestaurantRepo.findAllByZip(zip)
				.switchIfEmpty(Mono.error(new ServerException("Zip does not exist.")));
	}

}
