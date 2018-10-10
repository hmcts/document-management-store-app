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
FROM openjdk:8-jre

MAINTAINER "HMCTS Evidence Team <https://github.com/hmcts>"
LABEL maintainer = "HMCTS Evidence Team <https://github.com/hmcts>"

WORKDIR /opt/app
COPY --from=builder /home/gradle/app/application/build/install/application .

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD http_proxy="" curl --silent --fail http://localhost:8080/health

EXPOSE 8080 5005

ENTRYPOINT ["/opt/app/bin/application"]
