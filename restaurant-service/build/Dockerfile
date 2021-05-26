FROM openjdk:11 
WORKDIR ./

ADD ./tmp ./
RUN jar -xf ./restaurant-service-0.0.1-SNAPSHOT.jar

EXPOSE 8101
ENTRYPOINT ["java","-cp","BOOT-INF/classes:BOOT-INF/lib/*","com.demoapp.foodorder.restaurant.configuration.RestaurantServiceApplication"]
