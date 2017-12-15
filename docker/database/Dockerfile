FROM postgres:9.6

MAINTAINER MAINTAINER "HMCTS Evidence Team <https://github.com/hmcts>"
LABEL maintainer=MAINTAINER "HMCTS Evidence Team <https://github.com/hmcts>"

COPY init-db.sh /docker-entrypoint-initdb.d

EXPOSE 5432
