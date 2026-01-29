package uk.gov.hmcts.dm.config.batch;

import com.azure.core.http.rest.PagedIterable;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobItem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.dm.domain.AuditActions;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.repository.StoredDocumentRepository;
import uk.gov.hmcts.dm.service.AuditEntryService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CaseDocumentsRecoveryTaskTest {

    @InjectMocks
    private CaseDocumentsRecoveryTask caseDocumentsRecoveryTask;

    @Mock
    private BlobContainerClient blobContainerClient;

    @Mock
    private StoredDocumentRepository storedDocumentRepository;

    @Mock
    private AuditEntryService auditEntryService;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(caseDocumentsRecoveryTask, "blobPath", "https://someblobpath");
    }

    @Test
    void shouldContinueIfThereIsNoFile() {

        PagedIterable pagedIterable = mock(PagedIterable.class);
        when(blobContainerClient.listBlobs()).thenReturn(pagedIterable);

        given(pagedIterable.stream())
                .willReturn(Stream.of());

        caseDocumentsRecoveryTask.run();

        verify(storedDocumentRepository, never()).save(any(StoredDocument.class));
        verify(auditEntryService, never()).createAndSaveEntry(any(StoredDocument.class), any(AuditActions.class));
        verify(blobContainerClient, never()).delete();

    }

    @Test
    void shouldContinueIfXlsxFileEmpty() {

        PagedIterable pagedIterable = mock(PagedIterable.class);
        when(blobContainerClient.listBlobs()).thenReturn(pagedIterable);

        BlobItem mockedBlobItem = mock(BlobItem.class);
        String deleteFileName = "CHG-12345.xlsx";
        given(mockedBlobItem.getName()).willReturn(deleteFileName);
        BlobClient mockedBlobClient = mock(BlobClient.class);
        given(blobContainerClient.getBlobClient(deleteFileName)).willReturn(mockedBlobClient);
        given(mockedBlobClient.getBlobName()).willReturn(deleteFileName);

        given(pagedIterable.stream())
                .willReturn(Stream.of(mockedBlobItem));

        caseDocumentsRecoveryTask.run();

        verify(mockedBlobClient).downloadToFile(System.getProperty("java.io.tmpdir") + "/recovered-documents.xlsx");
        verify(storedDocumentRepository, never()).findById(any(UUID.class));
        verify(storedDocumentRepository, never()).save(any());
        verify(mockedBlobClient).delete();
    }

    @Test
    void shouldSkipIfStoredDocumentsNotFound() throws IOException {
        Path filePath = Path.of(System.getProperty("java.io.tmpdir") + "/recovered-documents.xlsx");
        try {
            Path tempFile = Files.createFile(filePath);

            Files.write(tempFile, (UUID.randomUUID() + "," + UUID.randomUUID() + "\n"
                    + UUID.randomUUID() + "," + "SDSF-skip-this\n")
                    .getBytes(StandardCharsets.UTF_8));

            PagedIterable pagedIterable = mock(PagedIterable.class);

            when(blobContainerClient.listBlobs()).thenReturn(pagedIterable);

            BlobItem mockedBlobItem = mock(BlobItem.class);
            String deleteFileName = "EM-XYZ.csv";
            given(mockedBlobItem.getName()).willReturn(deleteFileName);
            BlobClient mockedBlobClient = mock(BlobClient.class);
            given(blobContainerClient.getBlobClient(deleteFileName)).willReturn(mockedBlobClient);
            given(mockedBlobClient.getBlobName()).willReturn(deleteFileName);

            given(pagedIterable.stream())
                    .willReturn(Stream.of(mockedBlobItem));

            Optional<StoredDocument> mockedOpt = mock(Optional.class);
            given(storedDocumentRepository.findById(any(UUID.class))).willReturn(mockedOpt);

            StoredDocument mockedStoredDocument = mock(StoredDocument.class);
            given(mockedOpt.get()).willReturn(mockedStoredDocument);
            given(mockedOpt.isEmpty()).willReturn(true);

            caseDocumentsRecoveryTask.run();

            verify(mockedBlobClient).downloadToFile(System.getProperty("java.io.tmpdir") + "/recovered-documents.xlsx");
            verify(storedDocumentRepository, times(3)).findById(any(UUID.class));
            verify(storedDocumentRepository, never()).save(any());
            verify(mockedBlobClient).delete();
        } finally {
            Files.deleteIfExists(filePath);
        }
    }

    @Test
    void shouldSkipIfDocumentContentVersionNotFound() throws IOException {
        Path filePath = Path.of(System.getProperty("java.io.tmpdir") + "/recovered-documents.xlsx");
        try {
            Path tempFile = Files.createFile(filePath);

            Files.write(tempFile, (UUID.randomUUID() + "," + UUID.randomUUID() + "\n"
                    + UUID.randomUUID() + "," + "SDSF-skip-this\n")
                    .getBytes(StandardCharsets.UTF_8));

            PagedIterable pagedIterable = mock(PagedIterable.class);

            when(blobContainerClient.listBlobs()).thenReturn(pagedIterable);

            BlobItem mockedBlobItem = mock(BlobItem.class);
            String deleteFileName = "EM-XYZ.csv";
            given(mockedBlobItem.getName()).willReturn(deleteFileName);
            BlobClient mockedBlobClient = mock(BlobClient.class);
            given(blobContainerClient.getBlobClient(deleteFileName)).willReturn(mockedBlobClient);
            given(mockedBlobClient.getBlobName()).willReturn(deleteFileName);

            given(pagedIterable.stream())
                    .willReturn(Stream.of(mockedBlobItem));

            Optional<StoredDocument> mockedOpt = mock(Optional.class);
            given(storedDocumentRepository.findById(any(UUID.class))).willReturn(mockedOpt);

            StoredDocument mockedStoredDocument = mock(StoredDocument.class);
            given(mockedOpt.get()).willReturn(mockedStoredDocument);
            given(mockedOpt.isEmpty()).willReturn(false);

            caseDocumentsRecoveryTask.run();

            verify(mockedBlobClient).downloadToFile(System.getProperty("java.io.tmpdir") + "/recovered-documents.xlsx");
            verify(storedDocumentRepository, times(3)).findById(any(UUID.class));
            verify(storedDocumentRepository, never()).save(any());
            verify(mockedBlobClient).delete();
        } finally {
            Files.deleteIfExists(filePath);
        }
    }

    @Test
    void shouldProcessAllValidDocumentIds() throws IOException {
        Path filePath = Path.of(System.getProperty("java.io.tmpdir") + "/recovered-documents.xlsx");
        try {
            Path tempFile = Files.createFile(filePath);
            UUID uuidRepeat = UUID.randomUUID();
            Files.write(tempFile, (uuidRepeat + "," + UUID.randomUUID() + "\n"
                    + UUID.randomUUID() + "," + uuidRepeat + ",SDSF-skip-this\n")
                    .getBytes(StandardCharsets.UTF_8));
            PagedIterable pagedIterable = mock(PagedIterable.class);

            when(blobContainerClient.listBlobs()).thenReturn(pagedIterable);

            BlobItem mockedBlobItem = mock(BlobItem.class);
            String deleteFileName = "INC23423.csv";
            given(mockedBlobItem.getName()).willReturn(deleteFileName);
            BlobClient mockedBlobClient = mock(BlobClient.class);
            given(blobContainerClient.getBlobClient(deleteFileName)).willReturn(mockedBlobClient);
            given(mockedBlobClient.getBlobName()).willReturn(deleteFileName);

            given(pagedIterable.stream())
                    .willReturn(Stream.of(mockedBlobItem));

            Optional<StoredDocument> mockedOpt = mock(Optional.class);
            given(storedDocumentRepository.findById(any(UUID.class))).willReturn(mockedOpt);
            StoredDocument mockedStoredDocument = new StoredDocument();
            DocumentContentVersion documentContentVersion = DocumentContentVersion.builder().id(uuidRepeat).build();
            mockedStoredDocument.setDocumentContentVersions(Arrays.asList(documentContentVersion));

            given(mockedOpt.get()).willReturn(mockedStoredDocument);
            given(mockedOpt.isEmpty()).willReturn(false);

            caseDocumentsRecoveryTask.run();

            verify(mockedBlobClient).downloadToFile(System.getProperty("java.io.tmpdir") + "/recovered-documents.xlsx");
            verify(storedDocumentRepository, times(3)).findById(any(UUID.class));
            verify(storedDocumentRepository, times(3)).save(any(StoredDocument.class));
            verify(auditEntryService, times(3)).createAndSaveEntry(any(StoredDocument.class),any(AuditActions.class));
            verify(mockedBlobClient).delete();

            Assertions.assertNotNull(mockedStoredDocument.getDocumentContentVersions().getFirst().getContentUri());
        } finally {
            Files.deleteIfExists(filePath);
        }
    }



}
