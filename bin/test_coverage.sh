#!/bin/sh
./gradlew jacocoTestReport --info

xdg-open build/reports/jacoco/test/html/index.html
open build/reports/jacoco/test/html/index.html

#./gradlew sonarqube -Dsonar.host.url=$SONARQUBE_URL
