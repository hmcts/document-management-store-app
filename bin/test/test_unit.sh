#!/bin/sh

#####################
# UNIT TEST #########
#####################
./gradlew check

xdg-open application/build/reports/checkstyle/main.html
open application/build/reports/checkstyle/main.html
start "" application/build/reports/checkstyle/main.html

xdg-open application/build/reports/checkstyle/test.html
open application/build/reports/checkstyle/test.html
start "" application/build/reports/checkstyle/test.html

xdg-open application/build/reports/pmd/main.html
open application/build/reports/pmd/main.html
start "" application/build/reports/pmd/main.html

xdg-open application/build/reports/pmd/test.html
open application/build/reports/pmd/test.html
start "" application/build/reports/pmd/test.html

xdg-open application/build/reports/tests/test/index.html
open application/build/reports/tests/test/index.html
start "" application/build/reports/tests/test/index.html
