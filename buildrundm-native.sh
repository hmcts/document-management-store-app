#!/bin/sh
#sudo apt-get install -y maven docker docker-compose
clear;
#      logging env vars
export ROOT_APPENDER=JSON_CONSOLE
export JSON_CONSOLE_PRETTY_PRINT=false
export REFORM_SERVICE_TYPE=java
export REFORM_SERVICE_NAME=document-management-store
export REFORM_TEAM=cc
export REFORM_ENVIRONMENT=local
#      healthcheck env vars
export PACKAGES_ENVIRONMENT=local
export PACKAGES_PROJECT=evidence
export PACKAGES_NAME=document-management-store
export PACKAGES_VERSION=unkown
#      debug mode
export JAVA_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"

./fakeversion.sh
./gradlew installDist bootRepackage

#./bin/test_dependency.sh

#./bin/test_coverage.sh

./build/install/document-management-store-app/bin/document-management-store-app
