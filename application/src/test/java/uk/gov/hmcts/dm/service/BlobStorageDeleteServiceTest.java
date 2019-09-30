package uk.gov.hmcts.dm.service;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.exception.FileStorageException;
import uk.gov.hmcts.dm.repository.DocumentContentVersionRepository;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.apache.tika.io.IOUtils.toInputStream;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CloudBlobContainer.class, CloudBlockBlob.class})
@PowerMockIgnore({"javax.net.ssl.*"})
public class BlobStorageDeleteServiceTest {

    private BlobStorageDeleteService blobStorageDeleteService;

    private CloudBlobContainer cloudBlobContainer;

    @Mock
    private MultipartFile file;
    @Mock
    private CloudBlockBlob blob;
    @Mock
    private DocumentContentVersionRepository documentContentVersionRepository;
    private static final String MOCK_DATA = "mock data";

    @Before
    public void setUp() throws Exception {
        cloudBlobContainer = PowerMockito.mock(CloudBlobContainer.class);
        blob = PowerMockito.mock(CloudBlockBlob.class);
        blobStorageDeleteService = new BlobStorageDeleteService(cloudBlobContainer, documentContentVersionRepository);
        try (final InputStream inputStream = toInputStream(MOCK_DATA)) {
            given(file.getInputStream()).willReturn(inputStream);
        }
    }

    @Test
    public void deleteDocumentContentVersion() throws Exception {
        final StoredDocument storedDocument = createStoredDocument();
        final DocumentContentVersion documentContentVersion = storedDocument.getDocumentContentVersions().get(0);
        given(cloudBlobContainer.getBlockBlobReference(documentContentVersion.getId().toString())).willReturn(blob);
        when(blob.deleteIfExists()).thenReturn(true);
        blobStorageDeleteService.deleteDocumentContentVersion(documentContentVersion);
        assertNull(documentContentVersion.getContentUri());
    }

    @Test
    public void deleteDocumentContentVersionDoesNotExist() throws Exception {
        final StoredDocument storedDocument = createStoredDocument();
        final DocumentContentVersion documentContentVersion = storedDocument.getDocumentContentVersions().get(0);
        documentContentVersion.setContentUri("x");
        given(cloudBlobContainer.getBlockBlobReference(documentContentVersion.getId().toString())).willReturn(blob);
        when(blob.deleteIfExists()).thenReturn(false);
        blobStorageDeleteService.deleteDocumentContentVersion(documentContentVersion);
        assertNotNull(documentContentVersion.getContentUri());
    }

    @Test(expected = FileStorageException.class)
    public void deleteDocumentContentVersionThrowsURISyntaxException() throws Exception {
        final StoredDocument storedDocument = createStoredDocument();
        final DocumentContentVersion documentContentVersion = storedDocument.getDocumentContentVersions().get(0);
        given(cloudBlobContainer.getBlockBlobReference(documentContentVersion.getId().toString()))
            .willThrow(new URISyntaxException("x", "y", 1));
        blobStorageDeleteService.deleteDocumentContentVersion(documentContentVersion);
    }

    @Test(expected = FileStorageException.class)
    public void deleteDocumentContentVersionThrowsStorageException() throws Exception {
        final StoredDocument storedDocument = createStoredDocument();
        final DocumentContentVersion documentContentVersion = storedDocument.getDocumentContentVersions().get(0);
        given(cloudBlobContainer.getBlockBlobReference(documentContentVersion.getId().toString()))
            .willThrow(new StorageException("x", "y", new RuntimeException("x")));
        blobStorageDeleteService.deleteDocumentContentVersion(documentContentVersion);
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
