FROM hmcts/cnp-java-base:openjdk-jre-8-alpine-1.2
LABEL maintainer="https://github.com/hmcts/document-management-store-api"

ENV APP dm-store.jar
ENV APPLICATION_TOTAL_MEMORY 768M
ENV APPLICATION_SIZE_ON_DISK_IN_MB 86

ENV JAVA_OPTS ""

COPY build/libs/$APP /opt/app/

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD http_proxy="" curl --silent --fail http://localhost:8080/health

EXPOSE 8080 5005
