#!/bin/sh
./gradlew check

xdg-open build/reports/checkstyle/main.html
open build/reports/checkstyle/main.html

xdg-open build/reports/checkstyle/test.html
open build/reports/checkstyle/test.html

xdg-open build/reports/pmd/main.html
open build/reports/pmd/main.html

xdg-open build/reports/pmd/test.html
open build/reports/pmd/test.html

xdg-open build/reports/tests/test/index.html
open build/reports/tests/test/index.html
