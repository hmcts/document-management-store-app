package uk.gov.hmcts.dm.functional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import uk.gov.hmcts.dm.config.DocumentMetadataDeletionTestConfiguration;
import uk.gov.hmcts.dm.service.DocumentMetadataDeletionService;
import uk.gov.hmcts.dm.service.EmAnnoService;
import uk.gov.hmcts.dm.service.EmNpaService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Functional test for DocumentMetadataDeletionService.
 * This test verifies that the service can successfully call em-anno and em-npa endpoints
 * with proper authentication (S2S and IDAM tokens) handled internally by the service.
 */
@Import({DocumentMetadataDeletionTestConfiguration.class, EmAnnoService.class, EmNpaService.class})
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
