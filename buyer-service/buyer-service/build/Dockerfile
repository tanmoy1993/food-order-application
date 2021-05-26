FROM openjdk:11 
WORKDIR ./

ADD ./tmp ./
RUN jar -xf ./buyer-service-0.0.1-SNAPSHOT.jar

EXPOSE 8092
ENTRYPOINT ["java","-cp","BOOT-INF/classes:BOOT-INF/lib/*","com.demoapp.foodorder.buyer.buyerservice.configuration.BuyerServiceApplication"]
