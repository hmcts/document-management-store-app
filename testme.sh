#!/bin/sh
#sudo apt-get install -y docker docker-compose
clear;
./gradlew clean assemble --info
./bin/test_dependency.sh
./bin/test_unit.sh
./bin/test_coverage.sh
./bin/test_integration.sh
