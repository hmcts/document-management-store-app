#!/bin/sh

IDAM_USER_BASE_URI=http://localhost:4501
IDAM_S2S_BASE_URI=http://localhost:4502
TEST_URL=http://localhost:4603

#####################
# PERFORMANCE TEST ##
#####################

./gradlew gatlingRun

xdg-open build/reports/gatling/*/index.html
open build/reports/gatling/*/index.html
