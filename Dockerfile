FROM openjdk:8-jre

RUN mkdir -p /usr/local/bin

COPY docker/lib/wait-for-it.sh /usr/local/bin
RUN chmod +x /usr/local/bin/wait-for-it.sh

COPY docker/entrypoint.sh /
COPY target/document-management-store-app-*.jar /app.jar

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD http_proxy= curl --silent --fail http://localhost:8080/health

EXPOSE 8080 5005

ENTRYPOINT [ "/entrypoint.sh" ]
