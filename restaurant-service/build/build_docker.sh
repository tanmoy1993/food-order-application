#!/bin/bash

sudo docker network create -d bridge my-bridge-network

sudo docker run -d --name my-cassandra --env-file ./all_configs.env --publish "7000:7000" -p "9042:9042"\
        --volume "data/cassandra:/var/lib/cassandra" \
        --memory 256MB --network my-bridge-network cassandra:3.11

# sudo docker network connect my-bridge-network my-cassandra

sleep 120

# sudo docker run -d --name my-postgres --env-file ./all_configs.env --publish "5432:5432" \
#         --volume "db_init:/docker-entrypoint-initdb.d" -v "pg_datadir:/var/lib/postgresql/data" \
#         --memory 256MB postgres:12-alpine

# sudo docker network connect my-bridge-network my-postgres

# sleep 30

# sudo docker run -d --name my-zookeeper --publish "2181:2181" \
#         --memory 128MB --network my-bridge-network wurstmeister/zookeeper

#sudo docker network connect my-bridge-network my-zookeeper

# sudo docker run -d --name my-kafka1 --env-file ./all_configs.env --publish "9092:9092" \
#         --volume "kafka_pers_dir:/var/run/docker.sock" --link my-zookeeper\
#         --network my-bridge-network --memory 512MB wurstmeister/kafka

#sudo docker network connect my-bridge-network my-kafka1

# sleep 120

# ./build.sh
sudo docker build -t "food_order_restaurant_image" .
sudo docker run -d --name food_order_restaurant --env-file ./all_configs.env --publish "8101:8101" \
             --memory 128MB --network my-bridge-network food_order_restaurant_image

             
