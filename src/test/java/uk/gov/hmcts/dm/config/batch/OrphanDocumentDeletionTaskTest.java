package uk.gov.hmcts.dm.config.batch;

import com.azure.core.http.rest.PagedIterable;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.service.StoredDocumentService;
import uk.gov.hmcts.dm.service.batch.AuditedStoredDocumentBatchOperationsService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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

class OrphanDocumentDeletionTaskTest {

    @InjectMocks
    private OrphanDocumentDeletionTask orphanDocumentDeletionTask;

    @Mock
    private BlobContainerClient blobContainerClient;

    @Mock
    private StoredDocumentService documentService;

    @Mock
    private AuditedStoredDocumentBatchOperationsService auditedStoredDocumentBatchOperationsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldContinueIfThereIsNoFile() {

        PagedIterable pagedIterable = mock(PagedIterable.class);
        when(blobContainerClient.listBlobs()).thenReturn(pagedIterable);

        given(pagedIterable.stream())
            .willReturn(Stream.of());
        orphanDocumentDeletionTask.execute();

        verify(auditedStoredDocumentBatchOperationsService, never())
            .hardDeleteStoredDocument(any());
    }


    @Test
    void shouldProcessAllValidDocumentIds() throws IOException {
        Path filePath = Path.of(System.getProperty("java.io.tmpdir") + "/orphan-document.csv");
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
            given(documentService.findOne(any())).willReturn(mockedOpt);
            StoredDocument mockedStoredDocument = mock(StoredDocument.class);
            given(mockedOpt.get()).willReturn(mockedStoredDocument);
            given(mockedOpt.isPresent()).willReturn(true);
            orphanDocumentDeletionTask.execute();
            verify(mockedBlobClient).downloadToFile(System.getProperty("java.io.tmpdir") + "/orphan-document.csv");
            verify(documentService, times(3)).findOne(any(UUID.class));
            verify(auditedStoredDocumentBatchOperationsService, times(3))
                .hardDeleteStoredDocument(mockedStoredDocument, "INC23423", "orphan-document-deletion");

            verify(mockedBlobClient).delete();
        } finally {
            Files.deleteIfExists(filePath);
        }
    }

    @Test
    void shouldContinueIfCsvFileEmpty() {

        PagedIterable pagedIterable = mock(PagedIterable.class);
        when(blobContainerClient.listBlobs()).thenReturn(pagedIterable);

        BlobItem mockedBlobItem = mock(BlobItem.class);
        String deleteFileName = "CHG-65975.csv";
        given(mockedBlobItem.getName()).willReturn(deleteFileName);
        BlobClient mockedBlobClient = mock(BlobClient.class);
        given(blobContainerClient.getBlobClient(deleteFileName)).willReturn(mockedBlobClient);
        given(mockedBlobClient.getBlobName()).willReturn(deleteFileName);

        given(pagedIterable.stream())
            .willReturn(Stream.of(mockedBlobItem));
        orphanDocumentDeletionTask.execute();
        verify(mockedBlobClient).downloadToFile(System.getProperty("java.io.tmpdir") + "/orphan-document.csv");
        verify(documentService, never()).findOne(any());
        verify(auditedStoredDocumentBatchOperationsService, never())
            .hardDeleteStoredDocument(any());
        verify(mockedBlobClient).delete();
    }

    @Test
    void shouldDeleteFileIfFileNameIsNotValid() {

        PagedIterable pagedIterable = mock(PagedIterable.class);
        when(blobContainerClient.listBlobs()).thenReturn(pagedIterable);

        BlobItem mockedBlobItem = mock(BlobItem.class);
        String deleteFileName = "EM65975.csv";
        given(mockedBlobItem.getName()).willReturn(deleteFileName);
        BlobClient mockedBlobClient = mock(BlobClient.class);
        given(blobContainerClient.getBlobClient(deleteFileName)).willReturn(mockedBlobClient);
        given(mockedBlobClient.getBlobName()).willReturn(deleteFileName);

        given(pagedIterable.stream())
            .willReturn(Stream.of(mockedBlobItem));
        orphanDocumentDeletionTask.execute();
        verify(mockedBlobClient,never()).downloadToFile(any());
        verify(documentService, never()).findOne(any());
        verify(auditedStoredDocumentBatchOperationsService, never())
            .hardDeleteStoredDocument(any());
        verify(mockedBlobClient).delete();
    }

    @Test
    void shouldSkipIfStoredDocumentsNotFound() throws IOException {
        Path filePath = Path.of(System.getProperty("java.io.tmpdir") + "/orphan-document.csv");
        try {
            Path tempFile = Files.createFile(filePath);

            Files.write(tempFile, (UUID.randomUUID() + "," + UUID.randomUUID() + "\n"
                + UUID.randomUUID() + "," + "SDSF-skip-this\n")
                .getBytes(StandardCharsets.UTF_8));

            PagedIterable pagedIterable = mock(PagedIterable.class);

            when(blobContainerClient.listBlobs()).thenReturn(pagedIterable);

            BlobItem mockedBlobItem = mock(BlobItem.class);
            String deleteFileName = "EM-608.csv";
            given(mockedBlobItem.getName()).willReturn(deleteFileName);
            BlobClient mockedBlobClient = mock(BlobClient.class);
            given(blobContainerClient.getBlobClient(deleteFileName)).willReturn(mockedBlobClient);
            given(mockedBlobClient.getBlobName()).willReturn(deleteFileName);

            given(pagedIterable.stream())
                .willReturn(Stream.of(mockedBlobItem));

            Optional<StoredDocument> mockedOpt = mock(Optional.class);
            given(documentService.findOne(any())).willReturn(mockedOpt);
            StoredDocument mockedStoredDocument = mock(StoredDocument.class);
            given(mockedOpt.get()).willReturn(mockedStoredDocument);
            given(mockedOpt.isPresent()).willReturn(false);
            orphanDocumentDeletionTask.execute();
            verify(mockedBlobClient).downloadToFile(System.getProperty("java.io.tmpdir") + "/orphan-document.csv");
            verify(documentService, times(3)).findOne(any());
            verify(auditedStoredDocumentBatchOperationsService, never())
                .hardDeleteStoredDocument(any());
            verify(mockedBlobClient).delete();
        } finally {
            Files.deleteIfExists(filePath);
        }
    }
}
