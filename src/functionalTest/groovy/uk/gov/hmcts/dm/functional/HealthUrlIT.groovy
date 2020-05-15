package uk.gov.hmcts.dm.functional

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner
import org.junit.Test
import org.junit.runner.RunWith

import static org.hamcrest.Matchers.equalTo

@RunWith(SpringIntegrationSerenityRunner.class)
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
