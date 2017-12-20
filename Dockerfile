FROM openjdk:8-jre

MAINTAINER "HMCTS Evidence Team <https://github.com/hmcts>"
LABEL maintainer = "HMCTS Evidence Team <https://github.com/hmcts>"

COPY build/install/document-management-store-app /opt/app/

WORKDIR /opt/app

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD http_proxy="" wget -q http://localhost:8080/health || exit 1

EXPOSE 8080 5005

ENTRYPOINT ["/opt/app/bin/document-management-store-app"]
