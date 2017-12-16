# Document Management Store App
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT) 
[![Build Status](https://travis-ci.org/hmcts/document-management-store-app.svg?branch=master)](https://travis-ci.org/hmcts/document-management-store-app) 
[![codecov](https://codecov.io/gh/hmcts/document-management-store-app/branch/master/graph/badge.svg)](https://codecov.io/gh/hmcts/document-management-store-app) 
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/04bae03fe36b43759ea4f2df7c48fd43)](https://www.codacy.com/app/HMCTS/document-management-store-app) 
[![Known Vulnerabilities](https://snyk.io/test/github/hmcts/document-management-store-app/badge.svg)](https://snyk.io/test/github/hmcts/document-management-store-app) 

Document Management is a backend service to store and retrieve documents it is apart of reforms common components.

### Tech

It uses:

* Java8
* Spring boot
* Junit, Mockito and SpringBootTest and Powermockito
* Maven
* [lombok project](https://projectlombok.org/) - Lombok project

### Plugins
* [lombok plugin](https://plugins.jetbrains.com/idea/plugin/6317-lombok-plugin) - Lombok IDEA plugin

## Quickstart
```bash
#Cloning repo and running though docker
git clone https://github.com/hmcts/document-management-store-app.git
cd document-management-store-app/
./buildrundm-docker.sh
```

```bash
#Run this script to aquire IDAM credentials required for DM API.
./idam.sh
```

### Integration
There is currently a Java Client available here:
https://github.com/hmcts/document-management-client

### Swagger UI
To view our REST API go to {HOST}:{PORT}/swagger-ui.html
> http://localhost:8080/swagger-ui.html

