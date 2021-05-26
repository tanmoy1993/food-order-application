# food-order-application
Dockerized microservice with JAVA and C# with REST endpoints and Kafka as message bus

Technological stack:
1. PostgreSQL
2. MongoDB
3. Kafka

Features:
- Each service has their own database and communicate to other services via Kafka topics.
- Automated functional testing in Python(Unit tests has been skipped)
- Docker image for each service

## Services:
### Buyer Service: 
Features: 
- Traditional Java web application with _Spring_
- Data persistence in _PostgreSQL_
- User or Buyer information persisted over _REST_ endpoints
- Information provided to other services via publishing messages over _Kafka_

### Restaurant Service:
Features: 
- Reactive Java web application with _Spring_ and _Reactor Core 3_
- Fully unblocking implementation
- Data persistence in _MongoDB_
- Restaurant information e.g. served food items persisted over _REST_ endpoints
- Asynchronous processing of messages in _Kafka_ for both publication and subscription 

### Order and Delivery Service:
Features:
- ASP.NET Core Web App
- Data persistence in _MongoDB_ via Entity Framework
- Asynchronous processing of messages in _Kafka_ 
