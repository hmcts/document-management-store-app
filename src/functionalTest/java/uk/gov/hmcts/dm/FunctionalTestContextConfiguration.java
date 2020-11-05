package uk.gov.hmcts.dm;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan("uk.gov.hmcts.dm.functional")
@PropertySource("classpath:application.yml")
public class FunctionalTestContextConfiguration {

}
