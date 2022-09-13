package uk.gov.hmcts.dm.smoke.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:application-smoke-test.yaml")
@ComponentScan("uk.gov.hmcts.dm.smoke")
public class SmokeTestContextConfiguration {
}
