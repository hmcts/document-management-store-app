#!/usr/bin/env bash

set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE USER evidence;
    CREATE DATABASE evidence
        WITH OWNER = evidence
        ENCODING ='UTF-8'
        CONNECTION LIMIT = -1;
EOSQL

psql -v ON_ERROR_STOP=1 --dbname=evidence --username "$POSTGRES_USER" <<-EOSQL
    CREATE SCHEMA evidence AUTHORIZATION evidence;
EOSQL

psql -v ON_ERROR_STOP=1 --dbname=evidence --username "$POSTGRES_USER" <<-EOSQL
    CREATE EXTENSION lo;
EOSQL
