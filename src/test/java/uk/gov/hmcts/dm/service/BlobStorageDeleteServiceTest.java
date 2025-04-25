package uk.gov.hmcts.dm.service;

import com.azure.core.http.rest.Response;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.DeleteSnapshotsOptionType;
import com.azure.storage.blob.specialized.BlockBlobClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;

import java.util.UUID;

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class BlobStorageDeleteServiceTest {

    private BlobStorageDeleteService blobStorageDeleteService;

    @Mock
    private BlobContainerClient cloudBlobContainer;

    @Mock
    private BlobClient blobClient;

    @Mock
    private BlockBlobClient blob;

    StoredDocument storedDocument;
    DocumentContentVersion documentContentVersion;

    private Response mockResponse = mock(Response.class);


    @BeforeEach
    public void setUp() {
        given(cloudBlobContainer.getBlobClient(any())).willReturn(blobClient);
        given(blobClient.getBlockBlobClient()).willReturn(blob);

        storedDocument = createStoredDocument();
        documentContentVersion = storedDocument.getDocumentContentVersions().get(0);

        documentContentVersion.setContentChecksum("");
        documentContentVersion.setContentUri("");

        blobStorageDeleteService = new BlobStorageDeleteService(cloudBlobContainer);
    }

    @Test
    void delete_documentContentVersion() {
        when(mockResponse.getStatusCode()).thenReturn(202);
        given(blob.exists()).willReturn(true);
        given(blob.deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, null, null, null))
            .willReturn(mockResponse);
        blobStorageDeleteService.deleteDocumentContentVersion(documentContentVersion);
        verify(blob).deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, null, null, null);
        assertNull(documentContentVersion.getContentChecksum());
        assertNull(documentContentVersion.getContentUri());

    }

    @Test
    void delete_documentContentVersion_if_responseCode_404() {
        when(mockResponse.getStatusCode()).thenReturn(404);
        when(blob.exists()).thenReturn(true);
        when(blob.deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, null, null, null))
            .thenReturn(mockResponse);
        blobStorageDeleteService.deleteDocumentContentVersion(documentContentVersion);
        assertNull(documentContentVersion.getContentChecksum());
        assertNull(documentContentVersion.getContentUri());
    }

    @Test
    void not_delete_documentContentVersion_if_responseCode_not_202_or_404() {
        when(mockResponse.getStatusCode()).thenReturn(409);
        when(blob.exists()).thenReturn(true);
        when(blob.deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, null, null, null))
            .thenReturn(mockResponse);
        blobStorageDeleteService.deleteDocumentContentVersion(documentContentVersion);
        assertNotNull(documentContentVersion.getContentChecksum());
        assertNotNull(documentContentVersion.getContentUri());
    }

    @Test
    void not_delete_DocumentContentVersion_if_blob_delete_fails_with_exception() {
        var blobStorageException = mock(BlobStorageException.class);
        when(blob.exists()).thenReturn(true);
        when(blobStorageException.getStatusCode()).thenReturn(409);
        when(blob.deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, null, null, null))
            .thenThrow(blobStorageException);
        blobStorageDeleteService.deleteDocumentContentVersion(documentContentVersion);
        assertNotNull(documentContentVersion.getContentChecksum());
        assertNotNull(documentContentVersion.getContentUri());
    }

    @Test
    void delete_documentContentVersion_if_delete_blob_fails_with_404() {
        var blobStorageException = mock(BlobStorageException.class);
        when(blob.exists()).thenReturn(true);
        when(blobStorageException.getStatusCode()).thenReturn(404);
        when(blob.deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, null, null, null))
            .thenThrow(blobStorageException);
        blobStorageDeleteService.deleteDocumentContentVersion(documentContentVersion);
        assertNull(documentContentVersion.getContentChecksum());
        assertNull(documentContentVersion.getContentUri());
    }

    @Test
    void delete_documentContentVersion_if_blob_not_exist() {
        when(blob.exists()).thenReturn(false);
        blobStorageDeleteService.deleteDocumentContentVersion(documentContentVersion);
        verify(blob, never()).deleteWithResponse(any(), any(), any(), any());
        assertNull(documentContentVersion.getContentChecksum());
        assertNull(documentContentVersion.getContentUri());
    }

    @Test
    void shouldLogErrorWhenBlobDeletionFails() {
        var blobStorageException = mock(BlobStorageException.class);
        when(blob.exists()).thenReturn(true);
        when(blobStorageException.getStatusCode()).thenReturn(500);
        when(blob.deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, null, null, null))
                .thenThrow(blobStorageException);

        blobStorageDeleteService.deleteCaseDocumentBinary(documentContentVersion);

        verify(blob).deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, null, null, null);
        assertNotNull(documentContentVersion.getContentChecksum());
        assertNotNull(documentContentVersion.getContentUri());
    }

    @Test
    void shouldLogSuccessWhenBlobDeletedSuccessfully() {
        when(mockResponse.getStatusCode()).thenReturn(202);
        when(blob.exists()).thenReturn(true);
        when(blob.deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, null, null, null))
                .thenReturn(mockResponse);

        blobStorageDeleteService.deleteCaseDocumentBinary(documentContentVersion);

        verify(blob).deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, null, null, null);
    }

    @Test
    void shouldLogErrorWhenBlobDoesNotExist() {
        when(blob.exists()).thenReturn(false);

        blobStorageDeleteService.deleteCaseDocumentBinary(documentContentVersion);

        verify(blob, never()).deleteWithResponse(any(), any(), any(), any());
    }

    @Test
    void shouldLogErrorWhenBlobDeletionThrowsGenericException() {
        when(blob.exists()).thenReturn(true);
        when(blob.deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, null, null, null))
                .thenThrow(new RuntimeException("Generic exception"));

        blobStorageDeleteService.deleteCaseDocumentBinary(documentContentVersion);

        verify(blob).deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, null, null, null);
    }

    private StoredDocument createStoredDocument() {
        return createStoredDocument(randomUUID());
    }

    private StoredDocument createStoredDocument(UUID documentContentVersionUuid) {
        storedDocument = new StoredDocument();
        storedDocument.setId(randomUUID());
        storedDocument.setDocumentContentVersions(
            singletonList(buildDocumentContentVersion(documentContentVersionUuid, storedDocument)));
        return storedDocument;
    }

    private DocumentContentVersion buildDocumentContentVersion(
        UUID documentContentVersionUuid,
        StoredDocument storedDocument
    ) {
        DocumentContentVersion doc = new DocumentContentVersion();
        doc.setId(documentContentVersionUuid);
        doc.setStoredDocument(storedDocument);
        doc.setSize(1L);
        return doc;
    }
}
