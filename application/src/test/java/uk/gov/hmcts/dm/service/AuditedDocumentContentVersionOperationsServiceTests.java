package uk.gov.hmcts.dm.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.dm.componenttests.TestUtil;
import uk.gov.hmcts.dm.domain.AuditActions;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.exception.DocumentContentVersionNotFoundException;
import uk.gov.hmcts.dm.service.thumbnail.DocumentThumbnailService;

import java.io.OutputStream;
import java.util.UUID;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuditedDocumentContentVersionOperationsServiceTests {

    @Mock
    private DocumentContentVersionService documentContentVersionService;

    @Mock
    private AuditEntryService auditEntryService;

    @Mock
    private BlobStorageReadService blobStorageReadService;

    @Mock
    private DocumentThumbnailService documentThumbnailService;

    @Mock
    private OutputStream outputStream;

    @InjectMocks
    private AuditedDocumentContentVersionOperationsService auditedDocumentContentVersionOperationsService;

    @Test
    public void testReadFileContentVersionBinary() {
        DocumentContentVersion documentContentVersion = new DocumentContentVersion();

        auditedDocumentContentVersionOperationsService.readDocumentContentVersionBinary(documentContentVersion, outputStream);

        verify(documentContentVersionService, times(1)).streamDocumentContentVersion(documentContentVersion, outputStream);
        verify(auditEntryService, times(1)).createAndSaveEntry(documentContentVersion, AuditActions.READ);
    }

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
        when(documentContentVersionService.findOne(uuid)).thenReturn(documentContentVersion);
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

        when(documentContentVersionService.findOne(uuid)).thenReturn(documentContentVersion);

        auditedDocumentContentVersionOperationsService.readDocumentContentVersion(uuid);

    }

    @Test(expected = DocumentContentVersionNotFoundException.class)
    public void testReadFileContentVersionThatDoesNotExists() {

        UUID uuid = UUID.randomUUID();

        when(documentContentVersionService.findOne(uuid)).thenReturn(null);

        auditedDocumentContentVersionOperationsService.readDocumentContentVersion(uuid);

    }

}
