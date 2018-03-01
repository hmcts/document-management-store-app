#!/bin/sh
#sudo apt-get install -y docker docker-compose
clear;
./gradlew clean assemble --info
#./bin/test/test_dependency.sh
./bin/test/test_unit.sh
./bin/test/test_coverage.sh
./bin/test/test_integration.sh
#./bin/test/test_smoke.sh
#./bin/test/test_functional.sh
#./bin/test/test_perfomance.sh
