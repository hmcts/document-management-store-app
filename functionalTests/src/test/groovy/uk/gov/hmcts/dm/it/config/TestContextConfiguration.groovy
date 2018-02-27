package uk.gov.hmcts.dm.it.config

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

/**
 * Created by pawel on 17/10/2017.
 */
@Configuration
@PropertySource("classpath:application-test.yaml")
@ComponentScan("uk.gov.hmcts.dm.it")
class TestContextConfiguration {
}
