package uk.gov.hmcts.dm.service;

import com.azure.core.http.rest.Response;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.DeleteSnapshotsOptionType;
import com.azure.storage.blob.specialized.BlockBlobClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;

import java.util.UUID;

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class BlobStorageDeleteServiceTest {

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


    @Before
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
    public void delete_documentContentVersion() {
        when(mockResponse.getStatusCode()).thenReturn(202);
        given(blob.exists()).willReturn(true);
        given(blob.deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, null, null, null))
            .willReturn(mockResponse);
        blobStorageDeleteService.deleteDocumentContentVersion(documentContentVersion);
        verify(blob).deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, null, null, null);
        Assert.assertNull(documentContentVersion.getContentChecksum());
        Assert.assertNull(documentContentVersion.getContentUri());

    }

    @Test
    public void delete_documentContentVersion_if_responseCode_404() {
        when(mockResponse.getStatusCode()).thenReturn(404);
        when(blob.exists()).thenReturn(true);
        when(blob.deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, null, null, null))
            .thenReturn(mockResponse);
        blobStorageDeleteService.deleteDocumentContentVersion(documentContentVersion);
        Assert.assertNull(documentContentVersion.getContentChecksum());
        Assert.assertNull(documentContentVersion.getContentUri());
    }

    @Test
    public void not_delete_documentContentVersion_if_responseCode_not_202_or_404() {
        when(mockResponse.getStatusCode()).thenReturn(409);
        when(blob.exists()).thenReturn(true);
        when(blob.deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, null, null, null))
            .thenReturn(mockResponse);
        blobStorageDeleteService.deleteDocumentContentVersion(documentContentVersion);
        Assert.assertNotNull(documentContentVersion.getContentChecksum());
        Assert.assertNotNull(documentContentVersion.getContentUri());
    }

    @Test
    public void not_delete_DocumentContentVersion_if_blob_delete_fails_with_exception() {
        var blobStorageException = mock(BlobStorageException.class);
        when(blob.exists()).thenReturn(true);
        when(blobStorageException.getStatusCode()).thenReturn(409);
        when(blob.deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, null, null, null))
            .thenThrow(blobStorageException);
        blobStorageDeleteService.deleteDocumentContentVersion(documentContentVersion);
        Assert.assertNotNull(documentContentVersion.getContentChecksum());
        Assert.assertNotNull(documentContentVersion.getContentUri());
    }

    @Test
    public void delete_documentContentVersion_if_delete_blob_fails_with_404() {
        var blobStorageException = mock(BlobStorageException.class);
        when(blob.exists()).thenReturn(true);
        when(blobStorageException.getStatusCode()).thenReturn(404);
        when(blob.deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, null, null, null))
            .thenThrow(blobStorageException);
        blobStorageDeleteService.deleteDocumentContentVersion(documentContentVersion);
        Assert.assertNull(documentContentVersion.getContentChecksum());
        Assert.assertNull(documentContentVersion.getContentUri());
    }

    @Test
    public void delete_documentContentVersion_if_blob_not_exist() {
        when(blob.exists()).thenReturn(false);
        blobStorageDeleteService.deleteDocumentContentVersion(documentContentVersion);
        verify(blob, never()).deleteWithResponse(any(), any(), any(), any());
        Assert.assertNull(documentContentVersion.getContentChecksum());
        Assert.assertNull(documentContentVersion.getContentUri());
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
