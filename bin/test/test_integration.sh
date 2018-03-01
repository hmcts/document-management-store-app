#!/bin/sh
#if java gradle
IDAM_USER_BASE_URI=http://localhost:4501
IDAM_S2S_BASE_URI=http://localhost:4502
DM_GW_BASE_URI=http://localhost:3603
DM_STORE_APP_BASE_URI=http://localhost:4603
TEST_TOKEN=$(./bin/idam/idam-get-user-token.sh user1a@test.com 123 http://localhost:4501)

echo ${TEST_TOKEN}

./gradlew clean
./gradlew installDist bootRepackage

docker-compose -f ./docker/compose/docker-compose-dm.yml \
-f ./docker/compose/docker-compose-dm-ports.yml \
-f ./docker/compose/docker-compose-em.yml \
-f ./docker/compose/docker-compose-em-ports.yml \
-f ./docker/compose/docker-compose-idam.yml \
-f ./docker/compose/docker-compose-idam-ports.yml \
down

docker-compose -f ./docker/compose/docker-compose-dm.yml \
-f ./docker/compose/docker-compose-dm-ports.yml \
-f ./docker/compose/docker-compose-em.yml \
-f ./docker/compose/docker-compose-em-ports.yml \
-f ./docker/compose/docker-compose-idam.yml \
-f ./docker/compose/docker-compose-idam-ports.yml \
pull

docker-compose -f ./docker/compose/docker-compose-dm.yml \
-f ./docker/compose/docker-compose-dm-ports.yml \
-f ./docker/compose/docker-compose-em.yml \
-f ./docker/compose/docker-compose-em-ports.yml \
-f ./docker/compose/docker-compose-idam.yml \
-f ./docker/compose/docker-compose-idam-ports.yml \
up -d --build

echo "Waiting for the docker to warm up"
#sleep 60s
wget --retry-connrefused --tries=120 --waitretry=1 -O /dev/null ${DM_STORE_APP_BASE_URI}/health

#####################
# SMOKE TEST ########
#####################
TEST_TOKEN=$TEST_TOKEN ./gradlew smoke --info

xdg-open smokeTests/build/reports/tests/smoke/index.html
open smokeTests/build/reports/tests/smoke/index.html

#####################
# INTERGATION TEST ##
#####################
./gradlew functional --info

xdg-open functionalTests/build/reports/tests/functional/index.html
open functionalTests/build/reports/tests/functional/index.html


docker-compose down
