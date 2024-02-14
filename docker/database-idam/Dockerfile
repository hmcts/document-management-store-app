FROM postgres:16.2

LABEL maintainer = "HMCTS Evidence Team <https://github.com/hmcts>"

COPY init-db.sh /docker-entrypoint-initdb.d

EXPOSE 5432
