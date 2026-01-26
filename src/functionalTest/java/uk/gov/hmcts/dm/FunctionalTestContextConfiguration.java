package uk.gov.hmcts.dm;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.dm.config.DocumentMetadataDeletionTestConfiguration;
import uk.gov.hmcts.dm.service.EmAnnoService;
import uk.gov.hmcts.dm.service.EmNpaService;

@Configuration
@ComponentScan(basePackages = {"uk.gov.hmcts.dm.functional"})
@TestPropertySource({"classpath:application.yml"})
@EnableConfigurationProperties
@Import({DocumentMetadataDeletionTestConfiguration.class, EmAnnoService.class, EmNpaService.class})
public class FunctionalTestContextConfiguration {

}
