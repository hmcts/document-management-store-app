ARG APP_INSIGHTS_AGENT_VERSION=2.5.1
FROM hmctspublic.azurecr.io/base/java:openjdk-11-distroless-1.4

COPY build/libs/dm-store.jar lib/applicationinsights-agent-2.5.1.jar lib/AI-Agent.xml /opt/app/

LABEL maintainer="https://github.com/hmcts/document-management-store-api"

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD http_proxy="" wget -q --spider http://localhost:8080/health || exit 1

EXPOSE 8080 5005

CMD ["dm-store.jar"]
