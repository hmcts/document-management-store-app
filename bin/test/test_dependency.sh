#!/bin/sh
./gradlew dependencyCheckAnalyze -DdependencyCheck.failBuild=false

xdg-open application/build/reports/dependency-check-report.html
open application/build/reports/dependency-check-report.html
start "" application/build/reports/dependency-check-report.html
