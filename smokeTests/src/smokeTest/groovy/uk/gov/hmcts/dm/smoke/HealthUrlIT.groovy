package uk.gov.hmcts.dm.smoke

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.junit4.SpringRunner

import static org.hamcrest.Matchers.equalTo

/**
 * Created by pawel on 13/10/2017.
 */
@RunWith(SpringRunner.class)
class HealthUrlIT extends BaseIT {

    @Test
    void "Check health"() {
        givenUnauthenticatedRequest()
            .expect()
                .body('status', equalTo('UP'))

                .body('diskSpace.status', equalTo('UP'))

                .body('db.status', equalTo('UP'))
            .when()
                .get('/health')
    }


}
