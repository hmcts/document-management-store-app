#!/bin/sh
#sudo apt-get install -y maven docker docker-compose
clear;
./fakeversion.sh
mvn clean package -Dmaven.test.skip=true
docker-compose pull
docker-compose -f docker-compose.yml -f docker-compose-dev.yml up --build
