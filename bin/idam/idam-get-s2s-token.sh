#!/bin/sh
echo $(curl -X "POST" "${2}/testing-support/lease" \
     -H 'Content-Type: application/json; charset=utf-8' \
     -d $'{"microservice": "${1}"}')
