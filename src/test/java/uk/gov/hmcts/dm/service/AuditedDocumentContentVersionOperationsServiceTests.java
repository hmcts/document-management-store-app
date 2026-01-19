package uk.gov.hmcts.dm.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.dm.domain.AuditActions;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.exception.DocumentContentVersionNotFoundException;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class AuditedDocumentContentVersionOperationsServiceTests {

    @Mock
    private DocumentContentVersionService documentContentVersionService;

    @Mock
    private AuditEntryService auditEntryService;

    @Mock
    private BlobStorageReadService blobStorageReadService;

    @InjectMocks
    private AuditedDocumentContentVersionOperationsService auditedDocumentContentVersionOperationsService;

    @Test
    void testReadFileContentVersionBinaryFromBlobStore() throws IOException {
        DocumentContentVersion documentContentVersion = new DocumentContentVersion();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        auditedDocumentContentVersionOperationsService.readDocumentContentVersionBinaryFromBlobStore(
            documentContentVersion, request, response);

        verify(blobStorageReadService).loadBlob(documentContentVersion, request, response);
        verify(auditEntryService).createAndSaveEntry(documentContentVersion, AuditActions.READ);
    }

    @Test
    void testReadFileContentVersionThatExists() {
        UUID uuid = UUID.randomUUID();
        StoredDocument storedDocument = new StoredDocument();
        storedDocument.setDeleted(false);

        DocumentContentVersion documentContentVersion = new DocumentContentVersion();
        documentContentVersion.setStoredDocument(storedDocument);

        when(documentContentVersionService.findById(uuid)).thenReturn(Optional.of(documentContentVersion));

        DocumentContentVersion result =
            auditedDocumentContentVersionOperationsService.readDocumentContentVersion(uuid);

        assertNotNull(result);
        verify(auditEntryService).createAndSaveEntry(documentContentVersion, AuditActions.READ);
    }

    @Test
    void testReadFileContentVersionThatBelongsToDeletedDocument() {
        UUID uuid = UUID.randomUUID();
        StoredDocument storedDocument = new StoredDocument();
        storedDocument.setDeleted(true);

        DocumentContentVersion documentContentVersion = new DocumentContentVersion();
        documentContentVersion.setStoredDocument(storedDocument);

        when(documentContentVersionService.findById(uuid)).thenReturn(Optional.of(documentContentVersion));

        assertThrows(DocumentContentVersionNotFoundException.class, () ->
            auditedDocumentContentVersionOperationsService.readDocumentContentVersion(uuid)
        );
    }

    @Test
    void testReadFileContentVersionThatDoesNotExists() {
        UUID uuid = UUID.randomUUID();

        when(documentContentVersionService.findById(uuid)).thenReturn(Optional.empty());

        assertThrows(DocumentContentVersionNotFoundException.class, () ->
            auditedDocumentContentVersionOperationsService.readDocumentContentVersion(uuid)
        );
    }
}
