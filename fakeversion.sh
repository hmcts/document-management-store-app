#!/bin/sh
mkdir -p application/src/main/resources/META-INF
echo "build.version=$(./gradlew -q printVersion)" > application/src/main/resources/META-INF/build-info.properties
echo "build.number=${BUILD_NUMBER:=local}" >> application/src/main/resources/META-INF/build-info.properties
echo "build.commit=$(git rev-parse --short HEAD)" >> application/src/main/resources/META-INF/build-info.properties
echo "build.date=$(date)" >> application/src/main/resources/META-INF/build-info.properties
