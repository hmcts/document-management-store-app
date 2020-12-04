package uk.gov.hmcts.dm.functional

import org.junit.Rule
import org.junit.Test
import uk.gov.hmcts.reform.em.test.retry.RetryRule

import static org.hamcrest.Matchers.equalTo

class HealthUrlIT extends BaseIT {

    @Rule
    public RetryRule retryRule = new RetryRule(3);

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
