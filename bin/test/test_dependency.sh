#!/bin/sh
./gradlew dependencyCheckAnalyze -DdependencyCheck.failBuild=false

xdg-open build/reports/dependency-check-report.html
open build/reports/dependency-check-report.html
