#!/bin/sh
./gradlew installDist bootRepackage

docker-compose down
docker-compose -f docker-compose-all.yml -f docker-compose-test.yml pull
docker-compose up -d --build
docker-compose -f docker-compose-all.yml -f docker-compose-test.yml run document-management-store-integration-tests
docker-compose down
