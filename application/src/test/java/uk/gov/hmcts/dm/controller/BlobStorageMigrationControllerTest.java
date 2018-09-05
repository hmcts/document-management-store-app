package uk.gov.hmcts.dm.controller;

import org.junit.Test;
import uk.gov.hmcts.dm.componenttests.ComponentTestBase;
import uk.gov.hmcts.dm.exception.DocumentContentVersionNotFoundException;

import java.util.UUID;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BlobStorageMigrationControllerTest extends ComponentTestBase {

    private final UUID documentId = UUID.randomUUID();
    private final UUID versionId = UUID.randomUUID();

    @Test
    public void migrateDocument() throws Exception {

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .post("/documents/" + documentId + "/versions/" + versionId + "/migrate")
            .andExpect(status().isNoContent());

        verify(blobStorageMigrationService).migrateDocumentContentVersion(versionId);
    }

    @Test
    public void migrateNonExistingDocumentWillReturn404() throws Exception {

        doThrow(new DocumentContentVersionNotFoundException(versionId))
            .when(this.blobStorageMigrationService).migrateDocumentContentVersion(versionId);

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .post("/documents/" + documentId + "/versions/" + versionId + "/migrate")
            .andExpect(status().isNotFound());
    }
}
