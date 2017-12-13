#!/bin/sh
#sudo apt-get install -y curl jq
clear;
echo $(curl -s -H 'Content-Type: application/json' -d '{ "email":"test@TEST.COM", "forename":"test@TEST.COM","surname":"test@TEST.COM","password":"123"}' http://localhost:8081/testing-support/accounts)
echo "Authorization:"$(curl -s -X POST -H 'Authorization:Basic dGVzdEBURVNULkNPTToxMjM=' http://localhost:8081/oauth2/authorize | jq -r '."access-token"')
echo "ServiceAuthorization:"$(curl -s -d 'microservice=sscs' localhost:8082/testing-support/lease)
