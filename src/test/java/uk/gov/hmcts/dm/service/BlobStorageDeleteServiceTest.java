package uk.gov.hmcts.dm.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.implementation.models.BlobsDeleteResponse;
import com.azure.storage.blob.models.DeleteSnapshotsOptionType;
import com.azure.storage.blob.specialized.BlockBlobClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.repository.DocumentContentVersionRepository;

import java.util.UUID;

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
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

    @BeforeEach
    public void setUp() {
        blobStorageDeleteService = new BlobStorageDeleteService(cloudBlobContainer, documentContentVersionRepository);
        given(cloudBlobContainer.getBlobClient(any())).willReturn(blobClient);
        given(blobClient.getBlockBlobClient()).willReturn(blob);
    }

    @Test
    public void delete_documentContentVersion() {
        final StoredDocument storedDocument = createStoredDocument();
        final DocumentContentVersion documentContentVersion = storedDocument.getDocumentContentVersions().get(0);
        when(blob.deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, null, null, null))
            .thenReturn(new BlobsDeleteResponse(null, 202, null, null, null));
        blobStorageDeleteService.deleteDocumentContentVersion(documentContentVersion);
        verify(documentContentVersionRepository, times(1))
            .updateContentUriAndContentCheckSum(documentContentVersion.getId(), null, null);
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
