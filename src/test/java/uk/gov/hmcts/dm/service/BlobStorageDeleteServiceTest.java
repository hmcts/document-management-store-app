package uk.gov.hmcts.dm.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.implementation.models.BlobsDeleteResponse;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.DeleteSnapshotsOptionType;
import com.azure.storage.blob.specialized.BlockBlobClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.repository.DocumentContentVersionRepository;

import java.util.UUID;

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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

    @Mock
    private DocumentContentVersionRepository documentContentVersionRepository;

    @Before
    public void setUp() {

        given(cloudBlobContainer.getBlobClient(any())).willReturn(blobClient);
        given(blobClient.getBlockBlobClient()).willReturn(blob);

        blobStorageDeleteService = new BlobStorageDeleteService(cloudBlobContainer, documentContentVersionRepository);
    }

    @Test
    public void deleteDocumentContentVersion() {
        final StoredDocument storedDocument = createStoredDocument();
        final DocumentContentVersion documentContentVersion = storedDocument.getDocumentContentVersions().get(0);
        when(blob.deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, null, null, null))
            .thenReturn(new BlobsDeleteResponse(null, 202, null, null, null));
        blobStorageDeleteService.deleteDocumentContentVersion(documentContentVersion);
        assertNull(documentContentVersion.getContentUri());
    }

    @Test
    public void deleteDocumentContentVersionDoesNotExist() {
        final StoredDocument storedDocument = createStoredDocument();
        final DocumentContentVersion documentContentVersion = storedDocument.getDocumentContentVersions().get(0);
        documentContentVersion.setContentUri("x");
        when(blob.deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, null, null, null))
            .thenReturn(new BlobsDeleteResponse(null, 404, null, null, null));
        blobStorageDeleteService.deleteDocumentContentVersion(documentContentVersion);
        assertNotNull(documentContentVersion.getContentUri());
    }

    @Test
    public void notDeleteDocumentContentVersionDoesNotExistWithException() {
        final StoredDocument storedDocument = createStoredDocument();
        final DocumentContentVersion documentContentVersion = storedDocument.getDocumentContentVersions().get(0);
        documentContentVersion.setContentUri("x");
        var blobStorageException = mock(BlobStorageException.class);
        when(blobStorageException.getStatusCode()).thenReturn(409);
        when(blob.deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, null, null, null))
            .thenThrow(blobStorageException);
        blobStorageDeleteService.deleteDocumentContentVersion(documentContentVersion);
        verify(documentContentVersionRepository, never()).updateContentUriAndContentCheckSum(any(), any(), any());
        assertNotNull(documentContentVersion.getContentUri());
    }

    @Test
    public void deleteDocumentContentVersionDoesNotExistWithException() {
        final StoredDocument storedDocument = createStoredDocument();
        final DocumentContentVersion documentContentVersion = storedDocument.getDocumentContentVersions().get(0);
        documentContentVersion.setContentUri("x");
        var blobStorageException = mock(BlobStorageException.class);
        when(blobStorageException.getStatusCode()).thenReturn(404);
        when(blob.deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, null, null, null))
            .thenThrow(blobStorageException);
        blobStorageDeleteService.deleteDocumentContentVersion(documentContentVersion);
        verify(documentContentVersionRepository, times(1))
            .updateContentUriAndContentCheckSum(documentContentVersion.getId(), null, null);
        assertNotNull(documentContentVersion.getContentUri());
    }

    private StoredDocument createStoredDocument() {
        return createStoredDocument(randomUUID());
    }

    private StoredDocument createStoredDocument(UUID documentContentVersionUuid) {
        StoredDocument storedDocument = new StoredDocument();
        storedDocument.setId(randomUUID());
        storedDocument.setDocumentContentVersions(singletonList(buildDocumentContentVersion(documentContentVersionUuid,
            storedDocument)));
        return storedDocument;
    }

    private DocumentContentVersion buildDocumentContentVersion(UUID documentContentVersionUuid,
                                                               StoredDocument storedDocument) {
        DocumentContentVersion doc = new DocumentContentVersion();
        doc.setId(documentContentVersionUuid);
        doc.setStoredDocument(storedDocument);
        doc.setSize(1L);
        return doc;
    }
}
