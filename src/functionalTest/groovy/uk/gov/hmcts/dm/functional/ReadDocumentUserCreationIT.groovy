package uk.gov.hmcts.dm.functional;


import static org.hamcrest.Matchers.equalTo;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SpringIntegrationSerenityRunner.class)
public class ReadDocumentUserCreationIT extends BaseIT {

    @Test
    void "R26 userId provided during data creation can be obtained as username in the audit trail"() {

        def documentUrl = createDocumentAndGetUrlAs (CASE_WORKER)

        givenRequest(CASE_WORKER)
            .expect()
            .body("createdBy", equalTo(CASE_WORKER))
            .statusCode(200)
            .when()
            .get(documentUrl)


        String  userNameFromResponse  = givenRequest(CASE_WORKER, [CASE_WORKER_ROLE_PROBATE])
            .when()
            .get(documentUrl + "/auditEntries")
            .body().path('_embedded.auditEntries[0].username');

        assertThat(userNameFromResponse,equalTo(CASE_WORKER));
    }

}
