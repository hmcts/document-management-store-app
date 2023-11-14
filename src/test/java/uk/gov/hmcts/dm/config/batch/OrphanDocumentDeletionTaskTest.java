package uk.gov.hmcts.dm.config.batch;

import com.azure.core.http.rest.PagedIterable;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.service.StoredDocumentService;
import uk.gov.hmcts.dm.service.batch.AuditedStoredDocumentBatchOperationsService;

import java.io.File;
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

public class OrphanDocumentDeletionTaskTest {

    @InjectMocks
    private OrphanDocumentDeletionTask orphanDocumentDeletionTask;

    @Mock
    private BlobContainerClient blobClient;

    @Mock
    private StoredDocumentService documentService;

    @Mock
    private AuditedStoredDocumentBatchOperationsService auditedStoredDocumentBatchOperationsService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void shouldContinueIfThereIsnoFile() {

        PagedIterable pagedIterable = mock(PagedIterable.class);
        when(blobClient.listBlobs()).thenReturn(pagedIterable);

        given(pagedIterable.stream())
            .willReturn(Stream.of());
        orphanDocumentDeletionTask.execute();

        verify(auditedStoredDocumentBatchOperationsService, never())
            .hardDeleteStoredDocument(any());
    }


    @Test
    public void shouldProcessAllValidDocumentIds() throws IOException {
        Path tempFile = Files.createTempFile("test-file", ".csv");
        UUID uuidRepeat = UUID.randomUUID();
        Files.write(tempFile, (uuidRepeat + "," + UUID.randomUUID() + "\n"
            + UUID.randomUUID() + "," + uuidRepeat + ",SDSF-skip-this\n")
            .getBytes(StandardCharsets.UTF_8));
        File file = new File(tempFile.toUri());

        try (MockedStatic<File> mockedStatic = Mockito.mockStatic(File.class)) {
            mockedStatic.when(() -> File.createTempFile("orphan-document", ".csv")).thenReturn(file);
            PagedIterable pagedIterable = mock(PagedIterable.class);

            when(blobClient.listBlobs()).thenReturn(pagedIterable);

            BlobItem mockedBlobItem = mock(BlobItem.class);
            String deleteFileName = "delete-file.csv";
            given(mockedBlobItem.getName()).willReturn(deleteFileName);
            BlobClient mockedBlobClient = mock(BlobClient.class);
            given(blobClient.getBlobClient(deleteFileName)).willReturn(mockedBlobClient);

            given(pagedIterable.stream())
                .willReturn(Stream.of(mockedBlobItem));

            Optional<StoredDocument> mockedOpt = mock(Optional.class);
            given(documentService.findOne(any())).willReturn(mockedOpt);
            StoredDocument mockedStoredDocument = mock(StoredDocument.class);
            given(mockedOpt.get()).willReturn(mockedStoredDocument);
            given(mockedOpt.isPresent()).willReturn(true);
            orphanDocumentDeletionTask.execute();

            verify(documentService, times(3)).findOne(any(UUID.class));
            verify(auditedStoredDocumentBatchOperationsService, times(3))
                .hardDeleteStoredDocument(mockedStoredDocument);
        }
    }

    @Test
    public void shouldContinueIfCsvFileEmpty() {

        PagedIterable pagedIterable = mock(PagedIterable.class);
        when(blobClient.listBlobs()).thenReturn(pagedIterable);

        BlobItem mockedBlobItem = mock(BlobItem.class);
        String deleteFileName = "delete-file.csv";
        given(mockedBlobItem.getName()).willReturn(deleteFileName);
        BlobClient mockedBlobClient = mock(BlobClient.class);
        given(blobClient.getBlobClient(deleteFileName)).willReturn(mockedBlobClient);

        given(pagedIterable.stream())
            .willReturn(Stream.of(mockedBlobItem));
        orphanDocumentDeletionTask.execute();
        verify(documentService, never()).findOne(any());
        verify(auditedStoredDocumentBatchOperationsService, never())
            .hardDeleteStoredDocument(any());
    }

    @Test
    public void shouldSkipIfDocumentsNotFound() throws IOException {

        Path tempFile = Files.createTempFile("test-file", ".csv");

        Files.write(tempFile, (UUID.randomUUID() + "," + UUID.randomUUID() + "\n"
            + UUID.randomUUID() + "," + "SDSF-skip-this\n")
            .getBytes(StandardCharsets.UTF_8));
        File file = new File(tempFile.toUri());

        try (MockedStatic<File> mockedStatic = Mockito.mockStatic(File.class)) {
            mockedStatic.when(() -> File.createTempFile("orphan-document", ".csv")).thenReturn(file);

            PagedIterable pagedIterable = mock(PagedIterable.class);

            when(blobClient.listBlobs()).thenReturn(pagedIterable);

            BlobItem mockedBlobItem = mock(BlobItem.class);
            String deleteFileName = "delete-file.csv";
            given(mockedBlobItem.getName()).willReturn(deleteFileName);
            BlobClient mockedBlobClient = mock(BlobClient.class);
            given(blobClient.getBlobClient(deleteFileName)).willReturn(mockedBlobClient);

            given(pagedIterable.stream())
                .willReturn(Stream.of(mockedBlobItem));

            Optional<StoredDocument> mockedOpt = mock(Optional.class);
            given(documentService.findOne(any())).willReturn(mockedOpt);
            StoredDocument mockedStoredDocument = mock(StoredDocument.class);
            given(mockedOpt.get()).willReturn(mockedStoredDocument);
            given(mockedOpt.isPresent()).willReturn(false);
            orphanDocumentDeletionTask.execute();
            verify(documentService, times(3)).findOne(any());
            verify(auditedStoredDocumentBatchOperationsService, never())
                .hardDeleteStoredDocument(any());
        }
    }
}
