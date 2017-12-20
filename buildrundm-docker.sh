#!/bin/sh
#sudo apt-get install -y maven docker docker-compose
clear;
./fakeversion.sh
./gradlew installDist bootRepackage
docker-compose down
docker-compose pull
docker-compose -f docker-compose.yml -f docker-compose-dev.yml up --build
