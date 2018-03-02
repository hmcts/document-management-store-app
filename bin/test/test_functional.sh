#!/bin/sh

IDAM_USER_BASE_URI=http://localhost:4501
IDAM_S2S_BASE_URI=http://localhost:4502
TEST_URL=http://localhost:4603
TEST_TOKEN=$(./bin/idam/idam-get-user-token.sh user1a@test.com 123 http://localhost:4501)

#####################
# INTEGRATION TEST ##
#####################
./gradlew functional --info

xdg-open functionalTests/build/reports/tests/functional/index.html
open functionalTests/build/reports/tests/functional/index.html
