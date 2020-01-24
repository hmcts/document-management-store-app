package uk.gov.hmcts.dm.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.specialized.BlockBlobClient;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.data.domain.PageRequest;
import uk.gov.hmcts.dm.domain.*;
import uk.gov.hmcts.dm.exception.CantReadDocumentContentVersionBinaryException;
import uk.gov.hmcts.dm.exception.DocumentContentVersionNotFoundException;
import uk.gov.hmcts.dm.exception.DocumentNotFoundException;
import uk.gov.hmcts.dm.exception.FileStorageException;
import uk.gov.hmcts.dm.repository.DocumentContentVersionRepository;
import uk.gov.hmcts.dm.repository.MigrateEntryRepository;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.*;

import static java.nio.charset.Charset.defaultCharset;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.apache.commons.io.IOUtils.copy;
import static org.apache.tika.io.IOUtils.toInputStream;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.*;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.security.core.token.Sha512DigestUtils.shaHex;
import static uk.gov.hmcts.dm.domain.AuditActions.MIGRATED;
import static uk.gov.hmcts.dm.service.BlobStorageMigrationService.NO_CONTENT_FOUND;

@RunWith(PowerMockRunner.class)
@PrepareForTest({BlobContainerClient.class, BlockBlobClient.class})
@PowerMockIgnore({"javax.net.ssl.*"})
public class BlobStorageMigrationServiceTest {

    private BlobStorageMigrationService underTest;

    @Mock
    private StoredDocumentService storedDocumentService;
    @Mock
    private DocumentContentVersionService documentContentVersionService;
    @Mock
    private DocumentContentVersionRepository documentContentVersionRepository;
    private Blob data;
    @Mock
    private MigrateEntryRepository auditEntryRepository;
    @Mock
    private BatchMigrationTokenService batchMigrationTokenService;
    @Mock
    private BatchMigrationAuditEntryService batchMigrationAuditEntryService;
    @Mock
    private BatchMigrationAuditEntry batchmigrationAuditEntry;

    private BlobContainerClient cloudBlobContainer;
    private BlockBlobClient cloudBlockBlob;
    private BlobClient blob;
    private UUID documentContentVersionUuid;
    private UUID documentUuid;

    private static final String DOC_CONTENT = "!Where # is $ my % Herman ^ Miller Aeron?";
    private static final String DOC_CONTENT_CHECKSUM = shaHex(DOC_CONTENT.getBytes());

    @Before
    public void setUp() throws Exception {
        cloudBlobContainer = PowerMockito.mock(BlobContainerClient.class);
        blob = PowerMockito.mock(BlobClient.class);
        cloudBlockBlob = PowerMockito.mock(BlockBlobClient.class);

        when(cloudBlobContainer.getBlobClient(any())).thenReturn(blob);
        when(blob.getBlockBlobClient()).thenReturn(cloudBlockBlob);

        underTest = new BlobStorageMigrationService(cloudBlobContainer,
                                                    documentContentVersionRepository,
                                                    storedDocumentService,
                                                    auditEntryRepository,
                                                    batchMigrationTokenService,
                                                    batchMigrationAuditEntryService);
        documentContentVersionUuid = UUID.randomUUID();
        documentUuid = UUID.randomUUID();
        data = new SerialBlob(DOC_CONTENT.getBytes());
        when(batchMigrationAuditEntryService.createAuditEntry(any(), any(), any()))
            .thenReturn(batchmigrationAuditEntry);
    }

    @Test
    public void migrateDocumentContentVersion() throws Exception {
        DocumentContentVersion dcv = buildDocumentContentVersion();
        when(storedDocumentService.findOneWithBinaryData(documentUuid)).thenReturn(Optional.of(createStoredDocument()));
        when(documentContentVersionService.findById(documentContentVersionUuid)).thenReturn(Optional.of(dcv));
        when(documentContentVersionRepository.findById(documentContentVersionUuid)).thenReturn(Optional.of(dcv));

        final String azureProvidedUri = mockAzureBlobUpload(dcv);

        underTest.migrateDocumentContentVersion(documentUuid, documentContentVersionUuid);

        verifyMigrateInteractions(dcv, azureProvidedUri);
        verify(documentContentVersionRepository).findById(documentContentVersionUuid);
    }

    @Test(expected = FileStorageException.class)
    public void migrateDocumentContentVersionChecksumFailed() throws Exception {
        DocumentContentVersion dcv = buildDocumentContentVersion();
        when(storedDocumentService.findOneWithBinaryData(documentUuid)).thenReturn(Optional.of(createStoredDocument()));
        when(documentContentVersionRepository.findById(documentContentVersionUuid)).thenReturn(Optional.of(dcv));

        cloudBlockBlob = PowerMockito.mock(BlockBlobClient.class);
        String azureProvidedUri = "someuri";
        when(cloudBlockBlob.getBlobUrl()).thenReturn(new URI(azureProvidedUri).toString());
        when(cloudBlobContainer.getBlobClient(dcv.getId().toString()).getBlockBlobClient()).thenReturn(cloudBlockBlob);

        underTest.migrateDocumentContentVersion(documentUuid, documentContentVersionUuid);
    }

    @Test
    public void migrateDocumentContentVersionWithNoDocumentContent() throws Exception {
        DocumentContentVersion dcv = buildDocumentContentVersion(false);
        when(storedDocumentService.findOneWithBinaryData(documentUuid)).thenReturn(Optional.of(createStoredDocument()));
        when(documentContentVersionRepository.findById(documentContentVersionUuid)).thenReturn(Optional.of(dcv));

        assertNull(dcv.getContentChecksum());
        underTest.migrateDocumentContentVersion(documentUuid, documentContentVersionUuid);
        assertThat(dcv.getContentChecksum(), is(NO_CONTENT_FOUND));
    }

    @Test
    public void migrateDocumentAlreadyMigrated() throws Exception {
        DocumentContentVersion dcv = buildDocumentContentVersion();
        dcv.setContentUri("Migrated");
        dcv.setContentChecksum("someCheckSum");

        when(storedDocumentService.findOneWithBinaryData(documentUuid)).thenReturn(Optional.of(createStoredDocument()));
        when(documentContentVersionRepository.findById(documentContentVersionUuid)).thenReturn(Optional.of(dcv));

        cloudBlockBlob = PowerMockito.mock(BlockBlobClient.class);
        when(cloudBlockBlob.getBlobUrl()).thenReturn(new URI("someuri").toString());
        when(cloudBlobContainer.getBlobClient(dcv.getId().toString()).getBlockBlobClient()).thenReturn(cloudBlockBlob);
        prepareDownloadStream();

        underTest.migrateDocumentContentVersion(documentUuid, documentContentVersionUuid);

        verify(documentContentVersionRepository).findById(documentContentVersionUuid);
        verifyNoInteractionWithPostgresAndAzureAfterMigrate();
        assertThat(dcv.getContentUri(), is("Migrated"));
        assertThat(dcv.getContentChecksum(), is("someCheckSum"));
    }

    @Test(expected = DocumentNotFoundException.class)
    public void migrateNonExistentDocument() {
        when(storedDocumentService.findOneWithBinaryData(documentUuid)).thenReturn(Optional.empty());

        underTest.migrateDocumentContentVersion(documentUuid, documentContentVersionUuid);
    }

    @Test(expected = DocumentContentVersionNotFoundException.class)
    public void migrateDocumentWithNonExistentDocumentContentVersion() throws Exception {
        when(storedDocumentService.findOneWithBinaryData(documentUuid)).thenReturn(Optional.of(createStoredDocument()));

        UUID invalidDocumentContentVersionId = UUID.randomUUID();
        underTest.migrateDocumentContentVersion(documentUuid, invalidDocumentContentVersionId);
    }

    @Test(expected = DocumentContentVersionNotFoundException.class)
    public void migrateNonExistentDocumentContentVersion() throws Exception {
        when(storedDocumentService.findOneWithBinaryData(documentUuid)).thenReturn(Optional.of(createStoredDocument()));
        when(documentContentVersionService.findById(documentContentVersionUuid)).thenReturn(null);

        cloudBlockBlob = PowerMockito.mock(BlockBlobClient.class);
        when(cloudBlockBlob.getBlobUrl()).thenReturn(new URI("someuri").toString());

        underTest.migrateDocumentContentVersion(documentUuid, documentContentVersionUuid);

        verifyNoInteractionWithPostgresAndAzureAfterMigrate();
    }

    @Test(expected = CantReadDocumentContentVersionBinaryException.class)
    public void migrateThrowsCantReadDocumentContentVersionBinaryException() throws Exception {

        DocumentContentVersion dcv = buildDocumentContentVersion();
        when(storedDocumentService.findOneWithBinaryData(documentUuid)).thenReturn(Optional.of(createStoredDocument()));
        when(documentContentVersionRepository.findById(documentContentVersionUuid)).thenReturn(Optional.of(dcv));

        final Blob badData = mock(Blob.class);
        dcv.getDocumentContent().setData(badData);
        when(badData.getBinaryStream()).thenThrow(new SQLException());

        underTest.migrateDocumentContentVersion(documentUuid, documentContentVersionUuid);
    }

    @Test
    public void batchMigrateEmptyDocumentContentVersionSet() {
        final BatchMigrateProgressReport report = underTest.batchMigrate(null, 5, 0, false);
        assertThat(report.getStatus(), is(OK));
        assertThat(report.getErrors(), is(nullValue()));
        assertTrue(report.getMigratedDocumentContentVersions().isEmpty());
        verify(batchMigrationAuditEntryService).createAuditEntry(null, 5, false);
        verify(batchMigrationAuditEntryService).save(batchmigrationAuditEntry, report);
    }

    @Test
    public void batchMigrateDocumentContentVersion() throws Exception {

        final List<DocumentContentVersion> dcvList = asList(buildDocumentContentVersion(),
                                                            buildDocumentContentVersion(),
                                                            buildDocumentContentVersion());
        final Map<UUID, String> uriMap = new HashMap<>();
        when(documentContentVersionRepository.findByContentChecksumIsNullAndDocumentContentIsNotNull(any())).thenReturn(
            dcvList);
        for (DocumentContentVersion dcv: dcvList) {
            uriMap.put(dcv.getId(), mockAzureBlobUpload(dcv));
        }

        final BatchMigrateProgressReport report = underTest.batchMigrate(null, 5, 0, false);

        assertThat(report.getStatus(), is(OK));
        assertThat(report.getErrors(), is(nullValue()));
        assertThat(report.getMigratedDocumentContentVersions().size(), is(3));
        report.getMigratedDocumentContentVersions()
            .forEach(dcv -> assertThat(dcv.getUri(), is(uriMap.get(dcv.getVersionId()))));

        for (DocumentContentVersion dcv: dcvList) {
            verifyMigrateInteractions(dcv, uriMap.get(dcv.getId()));
        }
        verify(batchMigrationAuditEntryService).createAuditEntry(null, 5, false);
        verify(batchMigrationAuditEntryService).save(batchmigrationAuditEntry, report);
        verifyBatchMigrateDocumentContentVersionRepositoryQueries(5);
    }

    @Test
    public void batchMigrateDocumentContentVersionDryRun() throws Exception {

        final List<DocumentContentVersion> dcvList = asList(buildDocumentContentVersion(),
                                                            buildDocumentContentVersion(),
                                                            buildDocumentContentVersion());
        when(documentContentVersionRepository.findByContentChecksumIsNullAndDocumentContentIsNotNull(any())).thenReturn(
            dcvList);

        final BatchMigrateProgressReport report = underTest.batchMigrate(null, 7, 0, true);
        assertThat(report.getStatus(), is(OK));
        assertThat(report.getErrors(), is(nullValue()));
        assertThat(report.getMigratedDocumentContentVersions().size(), is(3));
        report.getMigratedDocumentContentVersions()
            .forEach(dcv -> assertThat(dcv.getUri(), is(nullValue())));
        verify(batchMigrationAuditEntryService).createAuditEntry(null, 7, true);
        verify(batchMigrationAuditEntryService).save(batchmigrationAuditEntry, report);
        verifyBatchMigrateDocumentContentVersionRepositoryQueries(7);
    }

    @Test
    public void batchMigrateInvalidAuthToken() {
//        doThrow(new ValidationErrorException("Thx mate")).when(batchMigrationTokenService.checkAuthToken(anyString()));
//        when(batchMigrationTokenService.checkAuthToken("token"));
//        underTest.batchMigrate("token", 7, true);
    }

    private String mockAzureBlobUpload(final DocumentContentVersion dcv) throws URISyntaxException {
        cloudBlockBlob = PowerMockito.mock(BlockBlobClient.class);
        blob = PowerMockito.mock(BlobClient.class);
        final String azureProvidedUri = RandomStringUtils.randomAlphanumeric(32);
        when(cloudBlockBlob.getBlobUrl()).thenReturn(new URI(azureProvidedUri).toString());
        when(cloudBlobContainer.getBlobClient(dcv.getId().toString())).thenReturn(blob);
        when(blob.getBlockBlobClient()).thenReturn(cloudBlockBlob);
        prepareDownloadStream();
        return azureProvidedUri;
    }

    private void verifyNoInteractionWithPostgresAndAzureAfterMigrate() {
        verifyNoMoreInteractions(documentContentVersionRepository);
        verifyNoMoreInteractions(auditEntryRepository);
        verifyNoMoreInteractions(cloudBlockBlob);
    }

    private StoredDocument createStoredDocument() throws SQLException {
        return createStoredDocument(documentContentVersionUuid);
    }

    private StoredDocument createStoredDocument(UUID documentContentVersionUuid) throws SQLException {
        StoredDocument storedDocument = new StoredDocument();
        storedDocument.setId(documentUuid);
        storedDocument.setDocumentContentVersions(singletonList(buildDocumentContentVersion(documentContentVersionUuid,
                                                                                            storedDocument)));
        return storedDocument;
    }

    private DocumentContentVersion buildDocumentContentVersion() throws SQLException {
        return buildDocumentContentVersion(UUID.randomUUID(), createStoredDocument());
    }

    private DocumentContentVersion buildDocumentContentVersion(boolean hasContent) throws SQLException {
        return buildDocumentContentVersion(UUID.randomUUID(), createStoredDocument(), hasContent);
    }

    private DocumentContentVersion buildDocumentContentVersion(UUID documentContentVersionUuid,
                                                               StoredDocument storedDocument) throws SQLException {
        return buildDocumentContentVersion(documentContentVersionUuid, storedDocument, true);
    }

    private DocumentContentVersion buildDocumentContentVersion(UUID documentContentVersionUuid,
                                                               StoredDocument storedDocument,
                                                               boolean hasContent) throws SQLException {
        DocumentContentVersion doc = new DocumentContentVersion();
        doc.setId(documentContentVersionUuid);
        doc.setStoredDocument(storedDocument);
        if (hasContent) {
            doc.setDocumentContent(createDocumentContent());
        }
        doc.setSize(data.length());
        return doc;
    }

    private DocumentContent createDocumentContent() {
        DocumentContent dc = new DocumentContent();
        dc.setData(data);
        return dc;
    }

    private void prepareDownloadStream() {
        doAnswer(invocation -> {
            try (final InputStream inputStream = toInputStream(DOC_CONTENT);
                 final OutputStream outputStream = invocation.getArgument(0)
            ) {
                return copy(inputStream, outputStream);
            }
        }).when(cloudBlockBlob).download(any(OutputStream.class));
    }

    private void verifyBatchMigrateDocumentContentVersionRepositoryQueries(int pageSize) {
        verify(documentContentVersionRepository, times(2)).countByContentChecksumIsNull();
        verify(documentContentVersionRepository, times(2)).countByContentChecksumIsNotNull();
        verify(documentContentVersionRepository).findByContentChecksumIsNullAndDocumentContentIsNotNull(argThat(new PageRequestMatcher(pageSize)));
    }

    private void verifyMigrateInteractions(final DocumentContentVersion dcv, final String azureProvidedUri)
        throws IOException {
        verify(auditEntryRepository).saveAndFlush(argThat(new AuditEntryMatcher(dcv)));
        verify(documentContentVersionRepository).updateContentUriAndContentCheckSum(dcv.getId(),
                                                                                    azureProvidedUri,
                                                                                    DOC_CONTENT_CHECKSUM);
        verify(cloudBlockBlob).upload(argThat(new InputStreamMatcher(DOC_CONTENT)), eq(dcv.getSize()));
        assertThat(dcv.getContentUri(), is(azureProvidedUri));
        assertThat(dcv.getContentChecksum(), is(DOC_CONTENT_CHECKSUM));
    }

    class InputStreamMatcher implements ArgumentMatcher<InputStream> {

        private final String expectedResult;

        InputStreamMatcher(final String expectedResult) {
            this.expectedResult = expectedResult;
        }

        @SneakyThrows(IOException.class)
        @Override
        public boolean matches(InputStream inputStream) {

            String actual = IOUtils.toString(inputStream, defaultCharset());
            inputStream.reset();

            return actual.equals(expectedResult);
        }

    }


    class AuditEntryMatcher implements ArgumentMatcher<MigrateEntry> {

        private final DocumentContentVersion documentContentVersion;

        AuditEntryMatcher(final DocumentContentVersion documentContentVersion) {
            this.documentContentVersion = documentContentVersion;
        }

        @Override
        public boolean matches(final MigrateEntry item) {
            return item.getDocumentcontentversionId().equals(documentContentVersion.getId())
                && item.getServicename().equals("Batch Migration Service")
                && item.getType().equals("Migrate content")
                && item.getAction().equals(MIGRATED)
                && item.getStoreddocumentId().equals(documentContentVersion.getStoredDocument().getId());

        }
    }

    class PageRequestMatcher implements ArgumentMatcher<PageRequest> {

        private final int pageSize;

        PageRequestMatcher(int pageSize) {
            this.pageSize = pageSize;
        }

        @Override
        public boolean matches(final PageRequest pageRequest) {
            return pageRequest.getPageSize() == pageSize
                && pageRequest.getSort().getOrderFor("createdOn").getDirection().equals(DESC)
                ;
        }
    }
}
