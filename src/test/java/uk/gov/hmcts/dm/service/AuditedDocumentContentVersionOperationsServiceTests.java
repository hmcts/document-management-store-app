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

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


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
    public void testReadFileContentVersionBinaryFromBlobStore() throws IOException {
        DocumentContentVersion documentContentVersion = new DocumentContentVersion();
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        auditedDocumentContentVersionOperationsService.readDocumentContentVersionBinaryFromBlobStore(
            documentContentVersion, request, response);

        verify(blobStorageReadService, times(1)).loadBlob(documentContentVersion, request, response);
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
