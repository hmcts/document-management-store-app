#!/bin/sh
mvn clean compile test
mvn jacoco:report
xdg-open target/site/jacoco/index.html
open target/site/jacoco/index.html