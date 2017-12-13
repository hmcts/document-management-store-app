#!/bin/sh
mkdir -p src/main/resources/META-INF
#echo "build.version=$(./gradlew -q printVersion)" > src/main/resources/META-INF/build-info.properties
echo "build.version=$(mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version 2>/dev/null | grep -Ev '(^\[|Download\w+:)')" > src/main/resources/META-INF/build-info.properties
echo "build.number=${BUILD_NUMBER:=local}" >> src/main/resources/META-INF/build-info.properties
echo "build.commit=$(git rev-parse --short HEAD)" >> src/main/resources/META-INF/build-info.properties
echo "build.date=$(date)" >> src/main/resources/META-INF/build-info.properties