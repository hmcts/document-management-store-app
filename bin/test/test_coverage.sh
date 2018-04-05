#!/bin/sh
./gradlew jacocoTestReport --info

xdg-open application/build/reports/jacoco/test/html/index.html
open application/build/reports/jacoco/test/html/index.html
start "" application/build/reports/jacoco/test/html/index.html

#./gradlew sonarqube -Dsonar.host.url=$SONARQUBE_URL
