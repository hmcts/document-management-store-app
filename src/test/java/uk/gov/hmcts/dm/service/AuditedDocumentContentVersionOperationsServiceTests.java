package uk.gov.hmcts.dm.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.dm.componenttests.TestUtil;
import uk.gov.hmcts.dm.domain.AuditActions;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.exception.DocumentContentVersionNotFoundException;
import uk.gov.hmcts.dm.service.thumbnail.DocumentThumbnailService;

import java.io.OutputStream;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class AuditedDocumentContentVersionOperationsServiceTests {

    @Mock
    private DocumentContentVersionService documentContentVersionService;

    @Mock
    private AuditEntryService auditEntryService;

    @Mock
    private BlobStorageReadService blobStorageReadService;

    @Mock
    private DocumentThumbnailService documentThumbnailService;

    @InjectMocks
    private AuditedDocumentContentVersionOperationsService auditedDocumentContentVersionOperationsService;

    @Test
    public void testReadFileContentVersionBinaryFromBlobStore() {
        DocumentContentVersion documentContentVersion = new DocumentContentVersion();
        OutputStream outputStream = Mockito.mock(OutputStream.class);
        auditedDocumentContentVersionOperationsService.readDocumentContentVersionBinaryFromBlobStore(documentContentVersion, outputStream);

        verify(blobStorageReadService, times(1)).loadBlob(documentContentVersion, outputStream);
        verify(auditEntryService, times(1)).createAndSaveEntry(documentContentVersion, AuditActions.READ);
    }

    @Test
    public void testReadDocumentContentVersionThumbnail() {
        DocumentContentVersion documentContentVersion = TestUtil.DOCUMENT_CONTENT_VERSION;

        auditedDocumentContentVersionOperationsService.readDocumentContentVersionThumbnail(documentContentVersion);

        verify(auditEntryService, times(1)).createAndSaveEntry(documentContentVersion, AuditActions.READ);
        verify(documentThumbnailService, times(1)).generateThumbnail(documentContentVersion);
    }

    @Test
    public void testReadFileContentVersionThatExists() {
        StoredDocument storedDocument = new StoredDocument();
        DocumentContentVersion documentContentVersion = new DocumentContentVersion();
        documentContentVersion.setStoredDocument(storedDocument);
        UUID uuid = UUID.randomUUID();
        when(documentContentVersionService.findById(uuid)).thenReturn(Optional.of(documentContentVersion));
        auditedDocumentContentVersionOperationsService.readDocumentContentVersion(uuid);
        verify(auditEntryService, times(1)).createAndSaveEntry(documentContentVersion, AuditActions.READ);

    }

    @Test(expected = DocumentContentVersionNotFoundException.class)
    public void testReadFileContentVersionThatBelongsToDeletedDocument() {
        StoredDocument storedDocument = new StoredDocument();
        storedDocument.setDeleted(true);
        DocumentContentVersion documentContentVersion = new DocumentContentVersion();
        documentContentVersion.setStoredDocument(storedDocument);

        UUID uuid = UUID.randomUUID();

        when(documentContentVersionService.findById(uuid)).thenReturn(Optional.of(documentContentVersion));

        auditedDocumentContentVersionOperationsService.readDocumentContentVersion(uuid);

    }

    @Test(expected = DocumentContentVersionNotFoundException.class)
    public void testReadFileContentVersionThatDoesNotExists() {

        UUID uuid = UUID.randomUUID();

        when(documentContentVersionService.findById(uuid)).thenReturn(Optional.empty());

        auditedDocumentContentVersionOperationsService.readDocumentContentVersion(uuid);

    }

}
