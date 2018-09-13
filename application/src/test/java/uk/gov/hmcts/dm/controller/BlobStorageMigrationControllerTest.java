package uk.gov.hmcts.dm.controller;

import com.microsoft.azure.storage.StorageException;
import org.junit.Test;
import uk.gov.hmcts.dm.componenttests.ComponentTestBase;
import uk.gov.hmcts.dm.exception.DocumentContentVersionNotFoundException;
import uk.gov.hmcts.dm.exception.FileStorageException;

import java.util.UUID;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
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

        verify(blobStorageMigrationService).migrateDocumentContentVersion(documentId, versionId);
    }

    @Test
    public void migrateNonExistingDocumentWillReturn404() throws Exception {

        doThrow(new DocumentContentVersionNotFoundException(versionId))
            .when(this.blobStorageMigrationService).migrateDocumentContentVersion(documentId, versionId);

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .post("/documents/" + documentId + "/versions/" + versionId + "/migrate")
            .andExpect(status().isNotFound());
    }

    @Test
    public void failsWith500OnAzureBlobStoreStorageException() throws Exception {

        doThrow(new FileStorageException(new StorageException("404", "Message", mock(Exception.class)),
            documentId,
            versionId))
            .when(this.blobStorageMigrationService).migrateDocumentContentVersion(documentId, versionId);

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .post("/documents/" + documentId + "/versions/" + versionId + "/migrate")
            .andExpect(status().isInternalServerError());
    }

    @Test
    public void migrateFailsWith404OnBadlyFormattedDocumentId() throws Exception {

        String invalidUuid = "invalidUUID";

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .post("/documents/" + invalidUuid + "/versions/" + versionId + "/migrate")
            .andExpect(status().isBadRequest());
    }

    @Test
    public void migrateFailsWith404OnBadlyFormattedVersionId() throws Exception {

        String invalidUuid = "invalidUUID";

        restActions
            .withAuthorizedUser("userId")
            .withAuthorizedService("divorce")
            .post("/documents/" + documentId + "/versions/" + invalidUuid + "/migrate")
            .andExpect(status().isBadRequest());
    }
}
