package uk.gov.hmcts.dm.it.config

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@PropertySource("classpath:application.yaml")
@ComponentScan("uk.gov.hmcts.dm.it")
class TestContextConfiguration {
}
