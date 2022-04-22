FROM postgres:11

MAINTAINER "HMCTS Evidence Team <https://github.com/hmcts>"
LABEL maintainer = "HMCTS Evidence Team <https://github.com/hmcts>"

COPY init-db.sh /docker-entrypoint-initdb.d

EXPOSE 5432
