FROM openjdk:8-jre

MAINTAINER "HMCTS Evidence Team <https://github.com/hmcts>"
LABEL maintainer = "HMCTS Evidence Team <https://github.com/hmcts>"

WORKDIR /opt/app
COPY application/build/install/application .

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD http_proxy="" curl --silent --fail http://localhost:8080/health

EXPOSE 8080 5005

ENTRYPOINT ["/opt/app/bin/application"]
