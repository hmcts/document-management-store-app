#!/bin/sh

IDAM_API_URL=http://localhost:4501
S2S_URL=http://localhost:4502
TEST_URL=http://localhost:4603

#####################
# INTEGRATION TEST ##
#####################
./gradlew functional --info

xdg-open functionalTests/build/reports/tests/functional/index.html
open functionalTests/build/reports/tests/functional/index.html
start "" functionalTests/build/reports/tests/functional/index.html
