# Document Management Store App

Evidence Management is a backend service to store and retrieve documents it is apart of reforms common components.

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

