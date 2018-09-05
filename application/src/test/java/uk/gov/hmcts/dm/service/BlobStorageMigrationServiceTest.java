package uk.gov.hmcts.dm.service;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import uk.gov.hmcts.dm.domain.AuditActions;
import uk.gov.hmcts.dm.domain.DocumentContent;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.exception.CantReadDocumentContentVersionBinaryException;
import uk.gov.hmcts.dm.exception.DocumentContentVersionNotFoundException;
import uk.gov.hmcts.dm.exception.FileStorageException;
import uk.gov.hmcts.dm.repository.DocumentContentVersionRepository;

import java.io.InputStream;
import java.net.URI;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest( {CloudBlobContainer.class, CloudBlockBlob.class})
public class BlobStorageMigrationServiceTest {

    BlobStorageMigrationService underTest;

    @Mock
    private AuditEntryService auditEntryService;
    @Mock
    private DocumentContentVersionService documentContentVersionService;
    @Mock
    private DocumentContentVersionRepository documentContentVersionRepository;
    @Mock
    private Blob data;
    @Mock
    private InputStream is;

    private CloudBlobContainer cloudBlobContainer;
    private CloudBlockBlob blob;
    private UUID documentContentVersionUUID;


    @Before
    public void setUp() {
        cloudBlobContainer = PowerMockito.mock(CloudBlobContainer.class);
        underTest = new BlobStorageMigrationService(cloudBlobContainer,
            auditEntryService,
            documentContentVersionRepository,
            documentContentVersionService);
        documentContentVersionUUID = UUID.randomUUID();
    }

    @Test
    public void migrateDocumentContentVersion() throws Exception {

        DocumentContentVersion doc = buildDocument();

        when(documentContentVersionService.findOne(documentContentVersionUUID)).thenReturn(doc);
        when(data.getBinaryStream()).thenReturn(is);

        blob = PowerMockito.mock(CloudBlockBlob.class);
        when(blob.getUri()).thenReturn(new URI("someuri"));
        when(cloudBlobContainer.getBlockBlobReference(doc.getId().toString())).thenReturn(blob);

        underTest.migrateDocumentContentVersion(documentContentVersionUUID);

        verify(documentContentVersionRepository).save(doc);
        verify(auditEntryService).createAndSaveEntry(doc, AuditActions.UPDATED);

    }

    @Test(expected = DocumentContentVersionNotFoundException.class)
    public void migrateNonExistentDoc() throws Exception {

        when(documentContentVersionService.findOne(documentContentVersionUUID)).thenReturn(null);
        when(data.getBinaryStream()).thenReturn(is);

        blob = PowerMockito.mock(CloudBlockBlob.class);
        when(blob.getUri()).thenReturn(new URI("someuri"));

        underTest.migrateDocumentContentVersion(documentContentVersionUUID);

        verify(documentContentVersionRepository, never()).save(any(DocumentContentVersion.class));
        verify(auditEntryService, never()).createAndSaveEntry(any(DocumentContentVersion.class), AuditActions.UPDATED);

    }

    @Test(expected = DocumentContentVersionNotFoundException.class)
    public void migrateDeleted() throws Exception {

        DocumentContentVersion doc = buildDocument(true);

        when(documentContentVersionService.findOne(documentContentVersionUUID)).thenReturn(doc);
        when(data.getBinaryStream()).thenReturn(is);

        blob = PowerMockito.mock(CloudBlockBlob.class);
        when(blob.getUri()).thenReturn(new URI("someuri"));
        when(cloudBlobContainer.getBlockBlobReference(doc.getId().toString())).thenReturn(blob);

        try {
            underTest.migrateDocumentContentVersion(documentContentVersionUUID);
        } finally {
            verify(documentContentVersionRepository, never()).save(any(DocumentContentVersion.class));
            verify(auditEntryService, never()).createAndSaveEntry(any(DocumentContentVersion.class), eq(AuditActions.UPDATED));
        }
    }

    @Test(expected = FileStorageException.class)
    public void migrateThrowsExceptionOnUploadingTheBlob() throws Exception {

        DocumentContentVersion doc = buildDocument();

        when(documentContentVersionService.findOne(documentContentVersionUUID)).thenReturn(doc);
        when(data.getBinaryStream()).thenReturn(is);

        blob = PowerMockito.mock(CloudBlockBlob.class);
        PowerMockito.doThrow(new StorageException("404", "Message", mock(Exception.class)))
            .when(blob).upload(any(InputStream.class), anyLong());

        when(cloudBlobContainer.getBlockBlobReference(doc.getId().toString())).thenReturn(blob);

        underTest.migrateDocumentContentVersion(documentContentVersionUUID);
    }

    @Test(expected = CantReadDocumentContentVersionBinaryException.class)
    public void migrateThrowsCantReadDocumentContentVersionBinaryException() throws Exception {

        DocumentContentVersion doc = buildDocument();
        when(documentContentVersionService.findOne(documentContentVersionUUID)).thenReturn(doc);
        when(data.getBinaryStream()).thenThrow(new SQLException());

        underTest.migrateDocumentContentVersion(documentContentVersionUUID);
    }

    private DocumentContentVersion buildDocument() {
        return buildDocument(false);
    }

    private DocumentContentVersion buildDocument(boolean deleted) {
        DocumentContentVersion doc = new DocumentContentVersion();
        doc.setId(UUID.randomUUID());
        doc.setStoredDocument(createStoredDocument(deleted));
        doc.setDocumentContent(createDocumentContent());
        doc.setSize(1L);
        return doc;
    }

    private StoredDocument createStoredDocument(boolean deleted) {
        StoredDocument storedDoc = new StoredDocument();
        storedDoc.setDeleted(deleted);
        return storedDoc;
    }

    private DocumentContent createDocumentContent() {
        DocumentContent dc = new DocumentContent();
        dc.setData(data);
        return dc;
    }
}
