#!/bin/sh

IDAM_USER_BASE_URI=http://localhost:4501
IDAM_S2S_BASE_URI=http://localhost:4502
DM_GW_BASE_URI=http://localhost:3603
TEST_URL=http://localhost:4603
TEST_TOKEN=$(./bin/idam/idam-get-user-token.sh user1a@test.com 123 http://localhost:4501)

#####################
# PERFORMANCE TEST ##
#####################

#./gradlew performance
#./gradlew gatlingRun --info

#xdg-open build/reports/gatling/*/index.html
#open build/reports/gatling/*/index.html
