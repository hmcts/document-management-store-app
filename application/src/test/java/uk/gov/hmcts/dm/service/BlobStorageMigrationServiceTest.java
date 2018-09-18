package uk.gov.hmcts.dm.service;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import uk.gov.hmcts.dm.domain.DocumentContent;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.exception.CantReadDocumentContentVersionBinaryException;
import uk.gov.hmcts.dm.exception.DocumentContentVersionNotFoundException;
import uk.gov.hmcts.dm.exception.DocumentNotFoundException;
import uk.gov.hmcts.dm.exception.FileStorageException;
import uk.gov.hmcts.dm.repository.DocumentContentVersionRepository;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import static java.nio.charset.Charset.defaultCharset;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.dm.domain.AuditActions.UPDATED;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CloudBlobContainer.class, CloudBlockBlob.class})
public class BlobStorageMigrationServiceTest {

    private BlobStorageMigrationService underTest;

    @Mock
    private AuditEntryService auditEntryService;
    @Mock
    private StoredDocumentService storedDocumentService;
    @Mock
    private DocumentContentVersionRepository documentContentVersionRepository;

    private Blob data;

    @Mock
    private Blob badData;

    private CloudBlobContainer cloudBlobContainer;
    private CloudBlockBlob cloudBlockBlob;
    private UUID documentContentVersionUuid;
    private UUID documentUuid;

    protected static final String DOC_CONTENT = "!Where # is $ my % Herman ^ Miller Aeron?";

    @Before
    public void setUp() throws Exception {
        cloudBlobContainer = PowerMockito.mock(CloudBlobContainer.class);
        underTest = new BlobStorageMigrationService(cloudBlobContainer,
                                                    auditEntryService,
                                                    documentContentVersionRepository,
                                                    storedDocumentService);
        documentContentVersionUuid = UUID.randomUUID();
        documentUuid = UUID.randomUUID();
        data = new SerialBlob(DOC_CONTENT.getBytes());
    }

    @Test
    public void migrateDocumentContentVersion() throws Exception {
        DocumentContentVersion dcv = buildDocumentContentVersion();
        when(storedDocumentService.findOneWithBinaryData(documentUuid)).thenReturn(Optional.of(createStoredDocument()));
        when(documentContentVersionRepository.findOne(documentContentVersionUuid)).thenReturn(dcv);

        cloudBlockBlob = PowerMockito.mock(CloudBlockBlob.class);
        String azureProvidedUri = "someuri";
        when(cloudBlockBlob.getUri()).thenReturn(new URI(azureProvidedUri));
        when(cloudBlobContainer.getBlockBlobReference(dcv.getId().toString())).thenReturn(cloudBlockBlob);

        underTest.migrateDocumentContentVersion(documentUuid, documentContentVersionUuid);

        verify(documentContentVersionRepository).update(dcv.getId(), azureProvidedUri);
        verify(auditEntryService).createAndSaveEntry(dcv, UPDATED);
        verify(cloudBlockBlob).upload(argThat(new InputStreamMatcher(DOC_CONTENT)), eq(dcv.getSize()));
        assertThat(dcv.getContentUri(), is(azureProvidedUri));
        assertThat(dcv.getDocumentContent(), is(dcv.getDocumentContent()));
    }

    @Test
    public void migrateDocumentAlreadyMigrated() throws Exception {
        DocumentContentVersion dcv = buildDocumentContentVersion();
        dcv.setContentUri("Migrated");

        when(storedDocumentService.findOneWithBinaryData(documentUuid)).thenReturn(Optional.of(createStoredDocument()));
        when(documentContentVersionRepository.findOne(documentContentVersionUuid)).thenReturn(dcv);

        cloudBlockBlob = PowerMockito.mock(CloudBlockBlob.class);
        when(cloudBlockBlob.getUri()).thenReturn(new URI("someuri"));
        when(cloudBlobContainer.getBlockBlobReference(dcv.getId().toString())).thenReturn(cloudBlockBlob);

        underTest.migrateDocumentContentVersion(documentUuid, documentContentVersionUuid);

        verify(documentContentVersionRepository).findOne(documentContentVersionUuid);
        verifyNoInteractionWithPostgresAndAzureAfterMigrate();
        assertThat(dcv.getContentUri(), is("Migrated"));
        assertThat(dcv.getDocumentContent(), is(dcv.getDocumentContent()));
    }

    @Test(expected = DocumentNotFoundException.class)
    public void migrateNonExistentDocument() {
        when(storedDocumentService.findOneWithBinaryData(documentUuid)).thenReturn(Optional.empty());

        underTest.migrateDocumentContentVersion(documentUuid, documentContentVersionUuid);
    }

    @Test(expected = DocumentContentVersionNotFoundException.class)
    public void migrateDocumentWithNonExistentDocumentContentVersion() {
        when(storedDocumentService.findOneWithBinaryData(documentUuid)).thenReturn(Optional.of(createStoredDocument()));

        UUID invalidDocumentContentVersionId = UUID.randomUUID();
        underTest.migrateDocumentContentVersion(documentUuid, invalidDocumentContentVersionId);
    }

    @Test(expected = DocumentContentVersionNotFoundException.class)
    public void migrateNonExistentDocumentContentVersion() throws Exception {
        when(storedDocumentService.findOneWithBinaryData(documentUuid)).thenReturn(Optional.of(createStoredDocument()));
        when(documentContentVersionRepository.findOne(documentContentVersionUuid)).thenReturn(null);

        cloudBlockBlob = PowerMockito.mock(CloudBlockBlob.class);
        when(cloudBlockBlob.getUri()).thenReturn(new URI("someuri"));

        underTest.migrateDocumentContentVersion(documentUuid, documentContentVersionUuid);

        verifyNoInteractionWithPostgresAndAzureAfterMigrate();
    }

    @Test(expected = FileStorageException.class)
    public void migrateThrowsExceptionOnUploadingTheBlob() throws Exception {

        DocumentContentVersion dcv = buildDocumentContentVersion();
        when(storedDocumentService.findOneWithBinaryData(documentUuid)).thenReturn(Optional.of(createStoredDocument()));
        when(documentContentVersionRepository.findOne(documentContentVersionUuid)).thenReturn(dcv);

        cloudBlockBlob = PowerMockito.mock(CloudBlockBlob.class);
        PowerMockito.doThrow(new StorageException("404", "Message", mock(Exception.class)))
            .when(cloudBlockBlob).upload(any(InputStream.class), anyLong());

        when(cloudBlobContainer.getBlockBlobReference(dcv.getId().toString())).thenReturn(cloudBlockBlob);

        underTest.migrateDocumentContentVersion(documentUuid, documentContentVersionUuid);
    }

    @Test(expected = CantReadDocumentContentVersionBinaryException.class)
    public void migrateThrowsCantReadDocumentContentVersionBinaryException() throws Exception {

        DocumentContentVersion doc = buildDocumentContentVersion(badData);
        when(storedDocumentService.findOneWithBinaryData(documentUuid)).thenReturn(Optional.of(createStoredDocument()));
        when(documentContentVersionRepository.findOne(documentContentVersionUuid)).thenReturn(doc);
        when(badData.getBinaryStream()).thenThrow(new SQLException());

        underTest.migrateDocumentContentVersion(documentUuid, documentContentVersionUuid);
    }

    private void verifyNoInteractionWithPostgresAndAzureAfterMigrate() {
        verifyNoMoreInteractions(documentContentVersionRepository);
        verifyNoMoreInteractions(auditEntryService);
        verifyNoMoreInteractions(cloudBlockBlob);
    }

    private StoredDocument createStoredDocument() {
        return createStoredDocument(documentContentVersionUuid);
    }

    private StoredDocument createStoredDocument(UUID documentContentVersionUuid) {
        StoredDocument storedDocument = new StoredDocument();
        storedDocument.setId(documentUuid);
        storedDocument.setDocumentContentVersions(singletonList(buildDocumentContentVersion(documentContentVersionUuid,
                                                                                            storedDocument,
                                                                                            data)));
        return storedDocument;
    }

    private DocumentContentVersion buildDocumentContentVersion() {
        return buildDocumentContentVersion(data);
    }

    private DocumentContentVersion buildDocumentContentVersion(Blob blob) {
        return buildDocumentContentVersion(UUID.randomUUID(), createStoredDocument(), blob);
    }

    private DocumentContentVersion buildDocumentContentVersion(UUID documentContentVersionUuid,
                                                               StoredDocument storedDocument,
                                                               Blob blob) {
        DocumentContentVersion dcv = new DocumentContentVersion();
        dcv.setId(documentContentVersionUuid);
        dcv.setStoredDocument(storedDocument);
        dcv.setDocumentContent(createDocumentContent(dcv, blob));
        dcv.setSize(Long.valueOf(DOC_CONTENT.length()));
        return dcv;
    }

    private DocumentContent createDocumentContent(DocumentContentVersion dcv, Blob blob) {
        return new DocumentContent(dcv, blob);
    }

    class InputStreamMatcher extends ArgumentMatcher<InputStream> {

        private final String expectedResult;

        InputStreamMatcher(final String expectedResult) {
            this.expectedResult = expectedResult;
        }

        @SneakyThrows(IOException.class)
        @Override
        public boolean matches(Object item) {
            InputStream inputStream = (InputStream) item;

            String actual = IOUtils.toString(inputStream, defaultCharset());
            inputStream.reset();

            return actual.equals(expectedResult);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("InputStream containing ").appendValue(expectedResult);
        }
    }
}

