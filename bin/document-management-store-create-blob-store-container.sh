#!/bin/bash
## Usage: ./document-management-store-create-blob-store-container.sh containerName
##
## Create a container in Azurite - Blob Store emulator
## containerName - name of the container to create
##
containerName=${1:-hmctstestcontainer}

echo "$containerName"

curl -XPUT --verbose \
  http://127.0.0.1:10000/devstoreaccount1/${containerName}?restype=container

# list containers
# curl -X GET http://127.0.0.1:10000/devstoreaccount1?comp=list
