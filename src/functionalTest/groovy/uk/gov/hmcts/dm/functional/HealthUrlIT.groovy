package uk.gov.hmcts.dm.functional


import org.junit.Test

import static org.hamcrest.Matchers.equalTo

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
