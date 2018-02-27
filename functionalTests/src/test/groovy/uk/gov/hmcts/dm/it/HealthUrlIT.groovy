package uk.gov.hmcts.dm.it

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.junit4.SpringRunner

import static org.hamcrest.Matchers.hasKey

/**
 * Created by pawel on 13/10/2017.
 */
@RunWith(SpringRunner.class)
class HealthUrlIT extends BaseIT {

    @Test
    void "Check health"() {
        givenRequest()
            .expect()
                .body('', hasKey("status"))

                .body('', hasKey("idam"))
                .body('idam', hasKey("status"))

                .body('', hasKey("idamService"))
                .body('idamService', hasKey("status"))

                .body('', hasKey("dmStore"))
                .body('dmStore', hasKey("status"))

                .body('', hasKey("dmStoreBuildInfo"))
                .body('dmStoreBuildInfo', hasKey("status"))

                .body('', hasKey("emApiGateway"))
                .body('emApiGateway', hasKey("status"))

                .body('', hasKey("buildInfo"))
            .when()
                .get('/health')
    }


}
