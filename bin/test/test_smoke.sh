#!/bin/sh

IDAM_API_URL=http://localhost:4501
S2S_URL=http://localhost:4502
TEST_URL=http://localhost:4603

#####################
# SMOKE TEST ########
#####################
./gradlew smoke --info

xdg-open smokeTests/build/reports/tests/smoke/index.html
open smokeTests/build/reports/tests/smoke/index.html
start "" smokeTests/build/reports/tests/smoke/index.html
