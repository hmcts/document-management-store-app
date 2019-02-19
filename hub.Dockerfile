#### Create Jar
FROM gradle:jdk8 as builder

COPY . /home/gradle/app
USER root
RUN chown -R gradle:gradle /home/gradle/app

USER gradle
WORKDIR /home/gradle/app

RUN mkdir -p application/src/main/resources/META-INF \
&& echo "build.version=$(gradle -q printVersion)" > application/src/main/resources/META-INF/build-info.properties \
&& echo "build.number=${BUILD_NUMBER:=docker}" >> application/src/main/resources/META-INF/build-info.properties \
&& echo "build.commit=$(git rev-parse HEAD)" >> application/src/main/resources/META-INF/build-info.properties \
&& echo "build.date=$(date)" >> application/src/main/resources/META-INF/build-info.properties

RUN gradle installDist

#### Actual DockerFile
FROM hmcts/cnp-java-base:openjdk-8u181-jre-alpine3.8-1.0
LABEL maintainer="https://github.com/hmcts/document-management-store-api"

ENV APP dm-store.jar
ENV APPLICATION_TOTAL_MEMORY 940M
ENV APPLICATION_SIZE_ON_DISK_IN_MB 80

ENV JAVA_OPTS ""

COPY --from=builder /home/gradle/app/build/libs/$APP /opt/app/

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD http_proxy="" curl --silent --fail http://localhost:8080/health

EXPOSE 8080 5005
