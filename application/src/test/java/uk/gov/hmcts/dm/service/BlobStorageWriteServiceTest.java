package uk.gov.hmcts.dm.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.specialized.BlobOutputStream;
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
import static org.apache.tika.io.IOUtils.toInputStream;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest({BlobContainerClient.class, BlockBlobClient.class, BlobProperties.class})
@PowerMockIgnore({"javax.net.ssl.*"})
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

    @Before
    public void setUp() throws Exception {
        cloudBlobContainer = PowerMockito.mock(BlobContainerClient.class);
        blob = PowerMockito.mock(BlockBlobClient.class);
        blobClient = PowerMockito.mock(BlobClient.class);

        given(cloudBlobContainer.getBlobClient(any())).willReturn(blobClient);
        given(blobClient.getBlockBlobClient()).willReturn(blob);

        blobStorageWriteService = new BlobStorageWriteService(cloudBlobContainer, documentContentVersionRepository);
        try (final InputStream inputStream = toInputStream(MOCK_DATA)) {
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
            try (final InputStream inputStream = toInputStream(MOCK_DATA);
                 final OutputStream outputStream = invocation.getArgument(0)
            ) {
                return copy(inputStream, outputStream);
            }
        }).when(blob).download(any(OutputStream.class));

        // upload
        blobStorageWriteService.uploadDocumentContentVersion(storedDocument,
            documentContentVersion,
            file);

        assertThat(documentContentVersion.getContentUri(), is(azureProvidedUri));
        verify(blob).upload(file.getInputStream(), documentContentVersion.getSize());
    }

    @Test
    public void uploadLargeDocument() throws Exception {
        final StoredDocument storedDocument = createStoredDocument();
        final DocumentContentVersion documentContentVersion = PowerMockito.mock(DocumentContentVersion.class);
        given(documentContentVersion.getSize()).willReturn((long) 256 * 1024 * 1024);
        given(documentContentVersion.getId()).willReturn(storedDocument.getDocumentContentVersions().get(0).getId());

        String azureProvidedUri = "someuri";
        given(blob.getBlobUrl()).willReturn(new URI(azureProvidedUri).toString());
        doAnswer(invocation -> {
            try (final InputStream inputStream = file.getInputStream();
                 final OutputStream outputStream = invocation.getArgument(0)
            ) {
                return copy(inputStream, outputStream);
            }
        }).when(blob).download(any(OutputStream.class));

        BlobOutputStream outputStream = PowerMockito.mock(BlobOutputStream.class);
        given(blob.getBlobOutputStream()).willReturn(outputStream);

        // upload
        blobStorageWriteService.uploadDocumentContentVersion(storedDocument,
            documentContentVersion,
            file);

        verify(blob).getBlobOutputStream();
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
