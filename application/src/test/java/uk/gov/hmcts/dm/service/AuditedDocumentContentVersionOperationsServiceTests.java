package uk.gov.hmcts.dm.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.dm.domain.AuditActions;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.exception.DocumentContentVersionNotFoundException;

import java.io.OutputStream;
import java.util.UUID;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by pawel on 09/08/2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class AuditedDocumentContentVersionOperationsServiceTests {

    @Mock
    DocumentContentVersionService documentContentVersionService;

    @Mock
    AuditEntryService auditEntryService;

    @Mock
    BlobStorageReadService blobStorageReadService;

    @InjectMocks
    AuditedDocumentContentVersionOperationsService auditedDocumentContentVersionOperationsService;

    @Test
    public void testReadFileContentVersionBinary() {
        DocumentContentVersion documentContentVersion = new DocumentContentVersion();
        auditedDocumentContentVersionOperationsService.readDocumentContentVersionBinary(documentContentVersion);

        verify(documentContentVersionService, times(1)).streamDocumentContentVersion(documentContentVersion);
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
