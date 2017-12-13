#!/bin/sh
mvn dependency-check:check -Powasp
xdg-open target/dependency-check-report.html
open target/dependency-check-report.html