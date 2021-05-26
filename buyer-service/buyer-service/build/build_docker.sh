#!/bin/bash

sudo docker network create -d bridge my-bridge-network

sudo docker run -d --name my-postgres --env-file ./all_configs.env --publish "5432:5432" \
        --volume "db_init:/docker-entrypoint-initdb.d" -v "pg_datadir:/var/lib/postgresql/data" \
        --memory 256MB postgres:12-alpine

sudo docker network connect my-bridge-network my-postgres

sleep 120

./build.sh
sudo docker build -t "food_order_buyer_image" .
sudo docker run -d --name food_order_buyer --env-file ./all_configs.env --publish "8092:8092" \
             --memory 128MB food_order_buyer_image

sudo docker network connect my-bridge-network food_order_buyer
             
