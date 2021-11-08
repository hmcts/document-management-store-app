# Document Management Store App
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Build Status](https://travis-ci.org/hmcts/document-management-store-app.svg?branch=master)](https://travis-ci.org/hmcts/document-management-store-app)
[![codecov](https://codecov.io/gh/hmcts/document-management-store-app/branch/master/graph/badge.svg)](https://codecov.io/gh/hmcts/document-management-store-app)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/04bae03fe36b43759ea4f2df7c48fd43)](https://www.codacy.com/app/HMCTS/document-management-store-app)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/04bae03fe36b43759ea4f2df7c48fd43)](https://www.codacy.com/app/HMCTS/document-management-store-app)
[![Known Vulnerabilities](https://snyk.io/test/github/hmcts/document-management-store-app/badge.svg)](https://snyk.io/test/github/hmcts/document-management-store-app)

Document Management is a backend service to store and retrieve documents.

### Tech

It uses:

* Java 11
* Spring boot
* Junit, Mockito and SpringBootTest and Powermockito
* Gradle
* [lombok project](https://projectlombok.org/) - Lombok project

### Plugins
* [lombok plugin](https://plugins.jetbrains.com/idea/plugin/6317-lombok-plugin) - Lombok IDEA plugin

#### Environment variables
The following environment variables are required:

| Name | Default | Description |
|------|---------|-------------|
| IDAM_USER_BASE_URI | - | Base URL for IdAM's User API service (idam-app). `http://localhost:4501` for the dockerised local instance or tunneled `dev` instance. |
| IDAM_S2S_BASE_URI | - | Base URL for IdAM's S2S API service (service-auth-provider). `http://localhost:4502` for the dockerised local instance or tunneled `dev` instance. |
| MAX_FILE_SIZE | 100MB | Max file size |

**Note:** The MAX_FILE_SIZE enforces limit on the document upload in the document management backend service.
Additionally, the max request content length (including file sizes) need to be configured for the IIS web server (on
Azure) via *maxAllowedContentLength* property for request filter in **web.config** (config file within source
repository).

## Setup
```bash
# Get the connection string for Azure Blob Store and put it in place of getOneFromPortalAzure in application.yaml
# Do not commit it!!!
azure:
  storage:
      connection-string: ${STORAGEACCOUNT_PRIMARY_CONNECTION_STRING:getOneFromPortalAzure}
```

```bash
# Cloning repo and running though docker
git clone https://github.com/hmcts/document-management-store-app.git
cd document-management-store-app/

az login
az acr login --name hmctspublic && az acr login --name hmctsprivate

docker-compose -f docker-compose-dev.yml pull
docker-compose -f docker-compose-dev.yml up -d


# Run application
./gradlew bootRun
```
### Integration
There is currently a Java Client available here:
https://github.com/hmcts/document-management-client

### Swagger UI
To view our REST API go to {HOST}:{PORT}/swagger-ui/
> http://localhost:4603/swagger-ui/

### API Endpoints
A list of our endpoints can be found here
> https://hmcts.github.io/reform-api-docs/swagger.html?url=https://hmcts.github.io/reform-api-docs/specs/document-management-store-app.json

### Functional Tests
To use the functional tests locally for large .mp4 and .doc files please first download them
from here : [Large Files](https://portal.azure.com/#blade/Microsoft_Azure_Storage/ContainerMenuBlade/overview/storageAccountId/%2Fsubscriptions%2Fbf308a5c-0624-4334-8ff8-8dca9fd43783%2FresourceGroups%2Fdm-store-sandbox%2Fproviders%2FMicrosoft.Storage%2FstorageAccounts%2Fdmstorefiles/path/dm-store-files/etag/%220x8D83471B8B0648C%22/defaultEncryptionScope/%24account-encryption-key/denyEncryptionScopeOverride//defaultId//publicAccessVal/None)
and place them under your resources folder document-management-store-app/src/functionalTest/resources
This would make them available to be used in the Functional Test -
```bash
MV1 (R1) As authenticated user I should not be able to upload files that exceed permitted sizes
```
- Ensure the @Pending annotation is removed before running the test locally.
- Revert your changes after tests are run  as these are for running the above F-Test on local only.

### Contract Tests (Pact)
For Executing the contract provider test  execute
```./gradlew contractTest```
For Publishing the verification results to broker execute
```./gradlew runProviderPactVerification```
