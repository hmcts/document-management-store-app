#!/bin/sh

IDAM_API_URL=http://localhost:4501
S2S_URL=http://localhost:4502
TEST_URL=http://localhost:4603

#####################
# PERFORMANCE TEST ##
#####################

./gradlew gatlingRun

xdg-open build/reports/gatling/*/index.html
open build/reports/gatling/*/index.html
start "" build/reports/gatling/*/index.html
