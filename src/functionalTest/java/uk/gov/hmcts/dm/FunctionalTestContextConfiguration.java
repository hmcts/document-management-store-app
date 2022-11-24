package uk.gov.hmcts.dm;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;

@Configuration
@ComponentScan(basePackages = {"uk.gov.hmcts.dm.functional"})
@TestPropertySource({"classpath:application.yml"})
@EnableConfigurationProperties
public class FunctionalTestContextConfiguration {

}
