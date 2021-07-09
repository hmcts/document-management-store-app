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
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
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
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({BlobContainerClient.class, BlockBlobClient.class, BlobStorageException.class})
@PowerMockIgnore({"javax.net.ssl.*", "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*"})
public class BlobStorageDeleteServiceTest {

    private BlobStorageDeleteService blobStorageDeleteService;

    private BlobContainerClient cloudBlobContainer;

    private BlobClient blobClient;

    @Mock
    private BlockBlobClient blob;
    @Mock
    private DocumentContentVersionRepository documentContentVersionRepository;

    @Before
    public void setUp() {
        cloudBlobContainer = PowerMockito.mock(BlobContainerClient.class);
        blob = PowerMockito.mock(BlockBlobClient.class);
        blobClient = PowerMockito.mock(BlobClient.class);

        given(cloudBlobContainer.getBlobClient(any())).willReturn(blobClient);
        given(blobClient.getBlockBlobClient()).willReturn(blob);

        blobStorageDeleteService = new BlobStorageDeleteService(cloudBlobContainer, documentContentVersionRepository);
    }

    @Test
    public void deleteDocumentContentVersion() throws Exception {
        final StoredDocument storedDocument = createStoredDocument();
        final DocumentContentVersion documentContentVersion = storedDocument.getDocumentContentVersions().get(0);
        when(blob.deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, null, null, null))
            .thenReturn(new BlobsDeleteResponse(null, 202, null, null, null));
        blobStorageDeleteService.deleteDocumentContentVersion(documentContentVersion);
        assertNull(documentContentVersion.getContentUri());
    }

    @Test
    public void deleteDocumentContentVersionDoesNotExist() throws Exception {
        final StoredDocument storedDocument = createStoredDocument();
        final DocumentContentVersion documentContentVersion = storedDocument.getDocumentContentVersions().get(0);
        documentContentVersion.setContentUri("x");
        when(blob.deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, null, null, null))
            .thenReturn(new BlobsDeleteResponse(null, 404, null, null, null));
        blobStorageDeleteService.deleteDocumentContentVersion(documentContentVersion);
        assertNotNull(documentContentVersion.getContentUri());
    }

    @Test
    public void deleteDocumentContentVersionDoesNotExistWithException() {
        final StoredDocument storedDocument = createStoredDocument();
        final DocumentContentVersion documentContentVersion = storedDocument.getDocumentContentVersions().get(0);
        documentContentVersion.setContentUri("x");
        when(blob.deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, null, null, null))
            .thenThrow(PowerMockito.mock(BlobStorageException.class));
        blobStorageDeleteService.deleteDocumentContentVersion(documentContentVersion);
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
