#!/usr/bin/env bash

set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE USER idam;
    CREATE DATABASE idam
        WITH OWNER = idam
        ENCODING ='UTF-8'
        CONNECTION LIMIT = -1;
EOSQL

psql -v ON_ERROR_STOP=1 --dbname=idam --username "$POSTGRES_USER" <<-EOSQL
    CREATE SCHEMA idam AUTHORIZATION idam;
EOSQL

psql -v ON_ERROR_STOP=1 --dbname=idam --username "$POSTGRES_USER" <<-EOSQL
    CREATE EXTENSION lo;
EOSQL
