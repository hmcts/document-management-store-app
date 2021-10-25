package uk.gov.hmcts.dm.smoke

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner
import net.thucydides.core.annotations.WithTag
import net.thucydides.core.annotations.WithTags
import org.junit.Test
import org.junit.runner.RunWith

import static org.hamcrest.Matchers.equalTo

@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags(@WithTag("testType:Smoke"))
class HealthUrlIT extends BaseIT {

    @Test
    void "Check health"() {
        givenUnauthenticatedRequest()
            .expect()
                .body('status', equalTo('UP'))

//                .body('diskSpace.status', equalTo('UP'))

//                .body('db.status', equalTo('UP'))
            .when()
                .get('/health')
    }


}
