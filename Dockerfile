FROM hmcts/cnp-java-base:openjdk-8u191-jre-alpine3.9-1.0
LABEL maintainer="https://github.com/hmcts/document-management-store-api"

ENV JAVA_OPTS ""

COPY build/libs/dm-store.jar /opt/app/

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD http_proxy="" curl --silent --fail http://localhost:8080/health

EXPOSE 4603 8080 5005

CMD ["dm-store.jar"]
