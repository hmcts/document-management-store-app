#!/bin/sh
B_T="Authorization: Basic "
USER_CRED=$(printf "${1}:${2}" | base64)
AUTH=$B_T$USER_CRED
echo $(curl -s -X POST -H "${AUTH}" ${3}/oauth2/authorize | jq -r '."access-token"')
