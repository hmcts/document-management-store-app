package uk.gov.hmcts.dm;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import uk.gov.hmcts.dm.functional.Toggles;

@Configuration
@ComponentScan("uk.gov.hmcts.dm.functional")
@PropertySource("classpath:application.yml")
@EnableAutoConfiguration
@EnableConfigurationProperties(Toggles.class)
class FunctionalTestContextConfiguration {

}
