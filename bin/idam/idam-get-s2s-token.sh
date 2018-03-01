#!/bin/sh
echo $(curl -s -d "microservice=${1}" ${2}/testing-support/lease)
