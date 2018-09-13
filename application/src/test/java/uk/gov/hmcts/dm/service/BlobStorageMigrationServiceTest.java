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
import uk.gov.hmcts.dm.exception.DocumentNotFoundException;
import uk.gov.hmcts.dm.exception.FileStorageException;
import uk.gov.hmcts.dm.repository.DocumentContentVersionRepository;
import uk.gov.hmcts.dm.repository.StoredDocumentRepository;

import java.io.InputStream;
import java.net.URI;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CloudBlobContainer.class, CloudBlockBlob.class})
public class BlobStorageMigrationServiceTest {

    private BlobStorageMigrationService underTest;

    @Mock
    private AuditEntryService auditEntryService;
    @Mock
    private StoredDocumentRepository storedDocumentRepository;
    @Mock
    private DocumentContentVersionRepository documentContentVersionRepository;
    @Mock
    private Blob data;
    @Mock
    private InputStream is;

    private CloudBlobContainer cloudBlobContainer;
    private CloudBlockBlob blob;
    private UUID documentContentVersionUuid;
    private UUID documentUuid;

    @Before
    public void setUp() {
        cloudBlobContainer = PowerMockito.mock(CloudBlobContainer.class);
        underTest = new BlobStorageMigrationService(cloudBlobContainer,
            auditEntryService,
            documentContentVersionRepository, storedDocumentRepository);
        documentContentVersionUuid = UUID.randomUUID();
        documentUuid = UUID.randomUUID();
    }

    @Test
    public void migrateDocumentContentVersion() throws Exception {
        DocumentContentVersion dcv = buildDocumentContentVersion();
        when(storedDocumentRepository.findOne(documentUuid)).thenReturn(createStoredDocument());
        when(documentContentVersionRepository.findOne(documentContentVersionUuid)).thenReturn(dcv);
        when(data.getBinaryStream()).thenReturn(is);

        blob = PowerMockito.mock(CloudBlockBlob.class);
        String azureProvidedUri = "someuri";
        when(blob.getUri()).thenReturn(new URI(azureProvidedUri));
        when(cloudBlobContainer.getBlockBlobReference(dcv.getId().toString())).thenReturn(blob);

        underTest.migrateDocumentContentVersion(documentUuid, documentContentVersionUuid);

        verify(documentContentVersionRepository).update(dcv.getId(), azureProvidedUri);
        verify(auditEntryService).createAndSaveEntry(dcv, AuditActions.UPDATED);
        verify(blob).upload(dcv.getDocumentContent().getData().getBinaryStream(), dcv.getSize());
        assertThat(dcv.getContentUri(), is(azureProvidedUri));
        assertThat(dcv.getDocumentContent(), is(dcv.getDocumentContent()));
    }

    @Test
    public void migrateDocumentAlreadyMigrated() throws Exception {
        DocumentContentVersion dcv = buildDocumentContentVersion();
        dcv.setContentUri("Migrated");

        when(storedDocumentRepository.findOne(documentUuid)).thenReturn(dcv.getStoredDocument());
        when(documentContentVersionRepository.findOne(documentContentVersionUuid)).thenReturn(dcv);
        when(data.getBinaryStream()).thenReturn(is);

        blob = PowerMockito.mock(CloudBlockBlob.class);
        when(blob.getUri()).thenReturn(new URI("someuri"));
        when(cloudBlobContainer.getBlockBlobReference(dcv.getId().toString())).thenReturn(blob);

        underTest.migrateDocumentContentVersion(documentUuid, documentContentVersionUuid);

        verify(documentContentVersionRepository).findOne(documentContentVersionUuid);
        verifyNoInteractionWithPostgresAndAzureAfterMigrate();
        assertThat(dcv.getContentUri(), is("Migrated"));
        assertThat(dcv.getDocumentContent(), is(dcv.getDocumentContent()));
    }

    @Test(expected = DocumentNotFoundException.class)
    public void migrateNonExistentDocument() {
        when(storedDocumentRepository.findOne(documentUuid)).thenReturn(null);

        underTest.migrateDocumentContentVersion(documentUuid, documentContentVersionUuid);
    }

    @Test(expected = DocumentContentVersionNotFoundException.class)
    public void migrateDocumentWithNonExistentDocumentContentVersion() {
        when(storedDocumentRepository.findOne(documentUuid)).thenReturn(createStoredDocument());

        UUID invalidDocumentContentVersionId = UUID.randomUUID();
        underTest.migrateDocumentContentVersion(documentUuid, invalidDocumentContentVersionId);
    }

    @Test(expected = DocumentContentVersionNotFoundException.class)
    public void migrateNonExistentDocumentContentVersion() throws Exception {
        when(storedDocumentRepository.findOne(documentUuid)).thenReturn(createStoredDocument());
        when(documentContentVersionRepository.findOne(documentContentVersionUuid)).thenReturn(null);
        when(data.getBinaryStream()).thenReturn(is);

        blob = PowerMockito.mock(CloudBlockBlob.class);
        when(blob.getUri()).thenReturn(new URI("someuri"));

        underTest.migrateDocumentContentVersion(documentUuid, documentContentVersionUuid);

        verifyNoInteractionWithPostgresAndAzureAfterMigrate();
    }

    @Test(expected = FileStorageException.class)
    public void migrateThrowsExceptionOnUploadingTheBlob() throws Exception {

        DocumentContentVersion doc = buildDocumentContentVersion();
        when(storedDocumentRepository.findOne(documentUuid)).thenReturn(createStoredDocument());
        when(documentContentVersionRepository.findOne(documentContentVersionUuid)).thenReturn(doc);
        when(data.getBinaryStream()).thenReturn(is);

        blob = PowerMockito.mock(CloudBlockBlob.class);
        PowerMockito.doThrow(new StorageException("404", "Message", mock(Exception.class)))
            .when(blob).upload(any(InputStream.class), anyLong());

        when(cloudBlobContainer.getBlockBlobReference(doc.getId().toString())).thenReturn(blob);

        underTest.migrateDocumentContentVersion(documentUuid, documentContentVersionUuid);
    }

    @Test(expected = CantReadDocumentContentVersionBinaryException.class)
    public void migrateThrowsCantReadDocumentContentVersionBinaryException() throws Exception {

        DocumentContentVersion doc = buildDocumentContentVersion();
        when(storedDocumentRepository.findOne(documentUuid)).thenReturn(createStoredDocument());
        when(documentContentVersionRepository.findOne(documentContentVersionUuid)).thenReturn(doc);
        when(data.getBinaryStream()).thenThrow(new SQLException());

        underTest.migrateDocumentContentVersion(documentUuid, documentContentVersionUuid);
    }

    private void verifyNoInteractionWithPostgresAndAzureAfterMigrate() {
        verifyNoMoreInteractions(documentContentVersionRepository);
        verifyNoMoreInteractions(auditEntryService);
        verifyNoMoreInteractions(blob);
    }

    private StoredDocument createStoredDocument() {
        return createStoredDocument(documentContentVersionUuid);
    }

    private StoredDocument createStoredDocument(UUID documentContentVersionUuid) {
        StoredDocument storedDocument = new StoredDocument();
        storedDocument.setId(documentUuid);
        storedDocument.setDocumentContentVersions(singletonList(buildDocumentContentVersion(documentContentVersionUuid,
                                                                                            storedDocument)));
        return storedDocument;
    }

    private StoredDocument createStoredDocument(boolean deleted) {
        StoredDocument storedDoc = new StoredDocument();
        storedDoc.setDeleted(deleted);
        return storedDoc;
    }

    private DocumentContentVersion buildDocumentContentVersion() {
        return buildDocumentContentVersion(UUID.randomUUID(), createStoredDocument());
    }

    private DocumentContentVersion buildDocumentContentVersion(UUID documentContentVersionUuid,
                                                               StoredDocument storedDocument) {
        DocumentContentVersion doc = new DocumentContentVersion();
        doc.setId(documentContentVersionUuid);
        doc.setStoredDocument(storedDocument);
        doc.setDocumentContent(createDocumentContent());
        doc.setSize(1L);
        return doc;
    }

    private DocumentContent createDocumentContent() {
        DocumentContent dc = new DocumentContent();
        dc.setData(data);
        return dc;
    }
}
