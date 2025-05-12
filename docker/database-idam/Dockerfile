FROM postgres:17.5

LABEL maintainer = "HMCTS Evidence Team <https://github.com/hmcts>"

COPY init-db.sh /docker-entrypoint-initdb.d

EXPOSE 5432
