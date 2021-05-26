package com.demoapp.foodorder.restaurant.configuration;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;

import com.mongodb.MongoClientSettings.Builder;
import com.mongodb.ServerAddress;

@Configuration
public class DBConf extends AbstractReactiveMongoConfiguration {

	@Override
	protected boolean autoIndexCreation() {
		return true;
	}

	@Override
	protected String getDatabaseName() {
		return "DS_FO_RESTAURANT";
	}

	@Override
	protected void configureClientSettings(Builder builder) {
		
		List<ServerAddress> server = new ArrayList<>();
		server.add(new ServerAddress("my-mongo", 27017));
		builder.applyToClusterSettings(settings -> {
					settings.hosts(server);
				});
	}

}