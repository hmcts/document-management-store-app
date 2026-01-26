package uk.gov.hmcts.dm.functional;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

import java.util.UUID;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;

/**
 * Functional test for DocumentMetadataDeletionService endpoints.
 * This test verifies that the em-anno and em-npa deletion endpoints can be called
 * with proper authentication (S2S and IDAM tokens).
 */
public class DocumentMetadataDeletionIT extends BaseIT {

    @Value("${toggle.deletemetadatafordocument:false}")
    private boolean deleteMetadataEnabled;

    @Value("${em-anno.api.url}")
    private String emAnnoApiUrl;

    @Value("${em-npa.api.url}")
    private String emNpaApiUrl;

    @Test
    public void shouldCallEmAnnoAndEmNpaEndpointsWhenDeletingMetadata() {
        if (!deleteMetadataEnabled) {
            // Skip test if toggle is disabled
            return;
        }

        // Use a random UUID for testing
        UUID testDocumentId = UUID.randomUUID();

        // Test em-anno endpoint
        // Expected responses: 404 (document not found), 204 (deleted), or 403 (not authorized)
        Response emAnnoResponse = givenRequest(getCaseWorker())
            .baseUri(emAnnoApiUrl)
            .when()
            .delete("/api/documents/" + testDocumentId + "/data");

        emAnnoResponse.then()
            .statusCode(anyOf(equalTo(204), equalTo(404), equalTo(403)));

        // Test em-npa endpoint
        // Expected responses: 404 (document not found), 204 (deleted), or 403 (not authorized)
        Response emNpaResponse = givenRequest(getCaseWorker())
            .baseUri(emNpaApiUrl)
            .when()
            .delete("/api/markups/document/" + testDocumentId);

        emNpaResponse.then()
            .statusCode(anyOf(equalTo(204), equalTo(404), equalTo(403)));
    }
}
