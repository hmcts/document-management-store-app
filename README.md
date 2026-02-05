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

* Java
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
# Cloning repo and running though docker
git clone https://github.com/hmcts/document-management-store-app.git
cd document-management-store-app/
```

#### Clean and build the application:
```
./gradlew clean
./gradlew build
```
To run just the unit tests:
```
./gradlew test
```
To run the integration tests:

Requires docker desktop running

```
./gradlew integration
```

#### To run the application:

Requires docker desktop running

```bash
docker-compose -f docker-compose-dev.yml pull
docker-compose -f docker-compose-dev.yml up -d

# Run application
./gradlew bootRun```
```

### Integration
There is currently a Java Client available here:
https://github.com/hmcts/document-management-client

### Swagger UI
To view our REST API go to http://{HOST}/swagger-ui/index.html
On local machine with server up and running, link to swagger is as below
> http://localhost:4603/swagger-ui/index.html
> if running on AAT, replace localhost with ingressHost data inside values.yaml class in the necessary component, making sure port number is also removed.

### API Endpoints
A list of our endpoints can be found here
> https://hmcts.github.io/cnp-api-docs/swagger.html?url=https://hmcts.github.io/cnp-api-docs/specs/document-management-store-app.json

### Contract Tests (Pact)
For executing the pact consumer test run

```./gradlew contract```

The results of the  consumer test will be published to the pact folder in the root directory of the project.

To run the provider pact tests, first comment the broker configuration
in the BaseProviderTest and StoredDocumentMultipartProviderTest classes and uncomment the pact folder configuration,
then run the below command to execute the provider pact tests locally.

```./gradlew providerContractTests```

