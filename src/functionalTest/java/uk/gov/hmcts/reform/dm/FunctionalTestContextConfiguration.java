package uk.gov.hmcts.reform.dm;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan("uk.gov.hmcts.reform.dm.functional")
@PropertySource("classpath:application.yml")
class FunctionalTestContextConfiguration {

}
