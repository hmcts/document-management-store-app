package uk.gov.hmcts.dm.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.specialized.BlockBlobClient;
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
import uk.gov.hmcts.dm.repository.DocumentContentVersionRepository;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.apache.commons.io.IOUtils.copy;
import static org.apache.commons.io.IOUtils.toInputStream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest({BlobContainerClient.class, BlockBlobClient.class, BlobProperties.class})
@PowerMockIgnore({"javax.net.ssl.*","com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*"})
public class BlobStorageWriteServiceTest {

    private BlobStorageWriteService blobStorageWriteService;

    private BlobContainerClient cloudBlobContainer;

    private BlobClient blobClient;

    @Mock
    private MultipartFile file;
    @Mock
    private BlockBlobClient blob;
    @Mock
    private DocumentContentVersionRepository documentContentVersionRepository;
    private static final String MOCK_DATA = "mock data";
    private static final String UTF8 = "UTF8";

    @Before
    public void setUp() throws Exception {
        cloudBlobContainer = PowerMockito.mock(BlobContainerClient.class);
        blob = PowerMockito.mock(BlockBlobClient.class);
        blobClient = PowerMockito.mock(BlobClient.class);

        given(cloudBlobContainer.getBlobClient(any())).willReturn(blobClient);
        given(blobClient.getBlockBlobClient()).willReturn(blob);

        blobStorageWriteService = new BlobStorageWriteService(cloudBlobContainer, documentContentVersionRepository);
        try (final InputStream inputStream = toInputStream(MOCK_DATA, UTF8)) {
            given(file.getInputStream()).willReturn(inputStream);
        }
    }

    @Test
    public void uploadDocumentContentVersion() throws Exception {
        final StoredDocument storedDocument = createStoredDocument();
        final DocumentContentVersion documentContentVersion = storedDocument.getDocumentContentVersions().get(0);
        String azureProvidedUri = "someuri";
        given(blob.getBlobUrl()).willReturn(new URI(azureProvidedUri).toString());

        doAnswer(invocation -> {
            try (final InputStream inputStream = toInputStream(MOCK_DATA, UTF8);
                 final OutputStream outputStream = invocation.getArgument(0)
            ) {
                return copy(inputStream, outputStream);
            }
        }).when(blob).downloadStream(any(OutputStream.class));

        // upload
        blobStorageWriteService.uploadDocumentContentVersion(storedDocument,
            documentContentVersion,
            file);

        assertThat(documentContentVersion.getContentUri(), is(azureProvidedUri));
        verify(blob).upload(any(), eq(documentContentVersion.getSize()));
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
