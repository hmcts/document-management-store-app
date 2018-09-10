package uk.gov.hmcts.dm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
@EnableCircuitBreaker
@EnableHystrixDashboard
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class DmApp {

    public static void main(String[] args) {
        SpringApplication.run(DmApp.class, args);
    }
}
