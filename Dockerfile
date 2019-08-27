# Keep hub.Dockerfile aligned to this file as far as possible
FROM hmctspublic.azurecr.io/base/java:openjdk-8-distroless-1.0

LABEL maintainer="https://github.com/hmcts/document-management-store-api"

ENV JAVA_OPTS ""

COPY build/libs/dm-store.jar /opt/app/

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD http_proxy="" wget -q --spider http://localhost:8080/health || exit 1

EXPOSE 8080 5005

CMD ["dm-store.jar"]
