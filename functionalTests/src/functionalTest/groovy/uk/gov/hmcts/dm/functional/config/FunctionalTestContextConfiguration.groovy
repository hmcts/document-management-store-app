package uk.gov.hmcts.dm.functional.config

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@PropertySource("classpath:application-functional-test.properties")
@ComponentScan("uk.gov.hmcts.dm.functional")
class FunctionalTestContextConfiguration {
}
