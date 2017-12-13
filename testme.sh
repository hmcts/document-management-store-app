#!/bin/sh
#sudo apt-get install -y maven docker docker-compose
clear;
mvn clean
./bin/test_dependency.sh
./bin/test_coverage.sh
./bin/test_integration.sh
