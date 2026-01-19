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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlobStorageDeleteServiceTest {

    @Mock
    private BlobContainerClient cloudBlobContainer;

    @Mock
    private BlobClient blobClient;

    @Mock
    private BlockBlobClient blockBlobClient;

    @Mock
    private Response<Void> mockResponse;

    @InjectMocks
    private BlobStorageDeleteService blobStorageDeleteService;

    private DocumentContentVersion documentContentVersion;

    @BeforeEach
    void setUp() {
        when(cloudBlobContainer.getBlobClient(anyString())).thenReturn(blobClient);
        when(blobClient.getBlockBlobClient()).thenReturn(blockBlobClient);

        StoredDocument storedDocument = new StoredDocument();
        storedDocument.setId(UUID.randomUUID());

        documentContentVersion = new DocumentContentVersion();
        documentContentVersion.setId(UUID.randomUUID());
        documentContentVersion.setStoredDocument(storedDocument);
        documentContentVersion.setContentUri("http://azure/blob");
        documentContentVersion.setContentChecksum("checksum123");
    }

    @Test
    void shouldDeleteDocumentContentVersionWhenResponseIs202() {
        when(blockBlobClient.exists()).thenReturn(true);
        when(blockBlobClient.deleteWithResponse(
            eq(DeleteSnapshotsOptionType.INCLUDE), isNull(), isNull(), isNull())
        ).thenReturn(mockResponse);
        when(mockResponse.getStatusCode()).thenReturn(202);

        blobStorageDeleteService.deleteDocumentContentVersion(documentContentVersion);

        verify(blockBlobClient).deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, null, null, null);
        assertNull(documentContentVersion.getContentChecksum());
        assertNull(documentContentVersion.getContentUri());
    }

    @Test
    void shouldDeleteDocumentContentVersionWhenResponseIs404() {
        when(blockBlobClient.exists()).thenReturn(true);
        when(blockBlobClient.deleteWithResponse(any(), any(), any(), any())).thenReturn(mockResponse);
        when(mockResponse.getStatusCode()).thenReturn(404);

        blobStorageDeleteService.deleteDocumentContentVersion(documentContentVersion);

        assertNull(documentContentVersion.getContentChecksum());
        assertNull(documentContentVersion.getContentUri());
    }

    @Test
    void shouldNotDeleteDocumentContentVersionFieldsWhenResponseIsNot202Or404() {
        when(blockBlobClient.exists()).thenReturn(true);
        when(blockBlobClient.deleteWithResponse(any(), any(), any(), any())).thenReturn(mockResponse);
        when(mockResponse.getStatusCode()).thenReturn(409);

        blobStorageDeleteService.deleteDocumentContentVersion(documentContentVersion);

        assertNotNull(documentContentVersion.getContentChecksum());
        assertNotNull(documentContentVersion.getContentUri());
    }

    @Test
    void shouldNotDeleteDocumentContentVersionFieldsWhenBlobDeleteThrowGenericException() {
        BlobStorageException blobStorageException = mock(BlobStorageException.class);
        when(blobStorageException.getStatusCode()).thenReturn(409);

        when(blockBlobClient.exists()).thenReturn(true);
        when(blockBlobClient.deleteWithResponse(any(), any(), any(), any()))
            .thenThrow(blobStorageException);

        blobStorageDeleteService.deleteDocumentContentVersion(documentContentVersion);

        assertNotNull(documentContentVersion.getContentChecksum());
        assertNotNull(documentContentVersion.getContentUri());
    }

    @Test
    void shouldClearDocumentContentVersionFieldsWhenBlobDeleteThrows404Exception() {
        BlobStorageException blobStorageException = mock(BlobStorageException.class);
        when(blobStorageException.getStatusCode()).thenReturn(404);

        when(blockBlobClient.exists()).thenReturn(true);
        when(blockBlobClient.deleteWithResponse(any(), any(), any(), any()))
            .thenThrow(blobStorageException);

        blobStorageDeleteService.deleteDocumentContentVersion(documentContentVersion);

        assertNull(documentContentVersion.getContentChecksum());
        assertNull(documentContentVersion.getContentUri());
    }

    @Test
    void shouldClearFieldsEvenIfBlobDoesNotExist() {
        when(blockBlobClient.exists()).thenReturn(false);

        blobStorageDeleteService.deleteDocumentContentVersion(documentContentVersion);

        verify(blockBlobClient, never()).deleteWithResponse(any(), any(), any(), any());

        assertNull(documentContentVersion.getContentChecksum());
        assertNull(documentContentVersion.getContentUri());
    }

    @Test
    void shouldLogAndContinueWhenDeleteCaseDocumentBinaryFailsWithException() {
        BlobStorageException blobStorageException = mock(BlobStorageException.class);

        when(blockBlobClient.exists()).thenReturn(true);
        when(blockBlobClient.deleteWithResponse(any(), any(), any(), any()))
            .thenThrow(blobStorageException);

        blobStorageDeleteService.deleteCaseDocumentBinary(documentContentVersion);

        verify(blockBlobClient).deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, null, null, null);
        assertNotNull(documentContentVersion.getContentChecksum());
    }

    @Test
    void shouldLogSuccessWhenDeleteCaseDocumentBinarySucceeds() {
        when(blockBlobClient.exists()).thenReturn(true);
        when(blockBlobClient.deleteWithResponse(any(), any(), any(), any())).thenReturn(mockResponse);
        when(mockResponse.getStatusCode()).thenReturn(202);

        blobStorageDeleteService.deleteCaseDocumentBinary(documentContentVersion);

        verify(blockBlobClient).deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, null, null, null);
    }

    @Test
    void shouldLogInfoWhenDeleteCaseDocumentBinaryFindsNoBlob() {
        when(blockBlobClient.exists()).thenReturn(false);

        blobStorageDeleteService.deleteCaseDocumentBinary(documentContentVersion);

        verify(blockBlobClient, never()).deleteWithResponse(any(), any(), any(), any());
    }

    @Test
    void shouldLogInfoWhenDeleteCaseDocumentBinaryThrowsGenericException() {
        when(blockBlobClient.exists()).thenReturn(true);
        when(blockBlobClient.deleteWithResponse(any(), any(), any(), any()))
            .thenThrow(new RuntimeException("Generic exception"));

        blobStorageDeleteService.deleteCaseDocumentBinary(documentContentVersion);

        verify(blockBlobClient).deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, null, null, null);
    }

    @Test
    void shouldLogInfoWhenDeleteCaseDocumentBinaryReturnsNonSuccessCode() {
        when(blockBlobClient.exists()).thenReturn(true);
        when(blockBlobClient.deleteWithResponse(any(), any(), any(), any())).thenReturn(mockResponse);
        when(mockResponse.getStatusCode()).thenReturn(409);

        blobStorageDeleteService.deleteCaseDocumentBinary(documentContentVersion);

        verify(blockBlobClient).deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, null, null, null);
        assertNotNull(documentContentVersion.getContentUri());
    }
}
