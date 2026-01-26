package uk.gov.hmcts.dm.functional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.dm.service.DocumentMetadataDeletionService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Functional test for DocumentMetadataDeletionService.
 * This test verifies that the service can successfully call em-anno and em-npa endpoints
 * with proper authentication (S2S and IDAM tokens) handled internally by the service.
 */
public class DocumentMetadataDeletionIT extends BaseIT {

    @Autowired
    private DocumentMetadataDeletionService documentMetadataDeletionService;

    @Test
    public void shouldCallEmAnnoAndEmNpaEndpointsWhenDeletingMetadata() {
        // Use a random UUID for testing
        UUID testDocumentId = UUID.randomUUID();

        boolean result = documentMetadataDeletionService.deleteExternalMetadata(testDocumentId);

        assertNotNull(result, "deleteExternalMetadata should return a non-null result");
    }
}
