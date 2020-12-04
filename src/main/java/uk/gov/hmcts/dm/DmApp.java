package uk.gov.hmcts.dm;

import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCircuitBreaker
@EnableHystrixDashboard
@EnableScheduling
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class DmApp {

    public static void main(String[] args) {
        //Setting Liquibase DB Lock property before Spring starts up.
        LiquibaseConfiguration.getInstance()
            .getConfiguration(GlobalConfiguration.class)
            .setUseDbLock(true);
        SpringApplication.run(DmApp.class, args);
    }
}
