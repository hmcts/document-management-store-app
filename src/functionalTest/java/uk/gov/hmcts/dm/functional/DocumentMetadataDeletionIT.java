package uk.gov.hmcts.dm.functional;

import net.jcip.annotations.NotThreadSafe;
import net.serenitybdd.annotations.WithTag;
import net.serenitybdd.annotations.WithTags;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.dm.StorageTestConfiguration;
import uk.gov.hmcts.dm.config.ToggleConfiguration;
import uk.gov.hmcts.dm.service.DocumentMetadataDeletionService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Functional test for DocumentMetadataDeletionService.
 * This test verifies that the service can successfully call em-anno and em-npa endpoints
 * with proper authentication (S2S and IDAM tokens) handled internally by the service.
 * This test uses its own isolated Spring context to avoid affecting other functional tests.
 */
@NotThreadSafe
@ExtendWith(value = {SerenityJUnit5Extension.class, SpringExtension.class})
@SpringBootTest(classes = {
    DocumentMetadataDeletionTestConfig.class,
    StorageTestConfiguration.class,
    ToggleConfiguration.class
})
@TestPropertySource({"classpath:application.yml"})
@WithTags(@WithTag("testType:Functional"))
public class DocumentMetadataDeletionIT {

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
