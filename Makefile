SHELL := /bin/bash

.PHONY: compile test package test-integration wait-for-startup

compile:
	mvn clean compile

test:
	mvn test

package:
	mvn package -DskipTests

test-integration:
	mvn clean test-compile failsafe:integration-test site failsafe:verify  -q

wait-for-startup:
	@echo "Waiting for the API to start"
	@while true; do if ! curl -s http://localhost:8080/health > /dev/null; then sleep 1; else break; fi; done


