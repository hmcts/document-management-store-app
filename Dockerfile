ARG APP_INSIGHTS_AGENT_VERSION=3.2.6
ARG PLATFORM=""

FROM hmctspublic.azurecr.io/base/java:17-distroless

COPY build/libs/dm-store.jar /opt/app/

LABEL maintainer="https://github.com/hmcts/document-management-store-api"

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD http_proxy="" wget -q --spider http://localhost:8080/health || exit 1

EXPOSE 8080 5005

CMD ["dm-store.jar"]
