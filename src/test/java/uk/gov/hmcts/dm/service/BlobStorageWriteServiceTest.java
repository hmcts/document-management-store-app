package uk.gov.hmcts.dm.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.specialized.BlockBlobClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.exception.FileStorageException;
import uk.gov.hmcts.dm.repository.DocumentContentVersionRepository;

import java.io.IOException;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
class BlobStorageWriteServiceTest {

    private BlobStorageWriteService blobStorageWriteService;

    @Mock
    private BlobContainerClient cloudBlobContainer;

    @Mock
    private BlobClient blobClient;

    @Mock
    private MultipartFile file;
    @Mock
    private BlockBlobClient blob;
    @Mock
    private DocumentContentVersionRepository documentContentVersionRepository;
    private static final String MOCK_DATA = "mock data";
    private static final String UTF8 = "UTF8";

    @BeforeEach
    public void setUp() throws Exception {

        given(cloudBlobContainer.getBlobClient(any())).willReturn(blobClient);
        given(blobClient.getBlockBlobClient()).willReturn(blob);

        blobStorageWriteService = new BlobStorageWriteService(cloudBlobContainer, documentContentVersionRepository);
        try (final InputStream inputStream = toInputStream(MOCK_DATA, UTF8)) {
            given(file.getInputStream()).willReturn(inputStream);
        }
    }

    @Test
    void uploadDocumentContentVersion() throws Exception {
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

    @Test
    void writeBinaryStreamThrowsFileStorageExceptionOnIOException() throws Exception {
        final StoredDocument storedDocument = createStoredDocument();
        final DocumentContentVersion documentContentVersion = storedDocument.getDocumentContentVersions().get(0);
        given(file.getInputStream()).willThrow(new IOException("Mocked IOException"));

        Exception exception = assertThrows(FileStorageException.class, () ->
                blobStorageWriteService.uploadDocumentContentVersion(storedDocument, documentContentVersion, file)
        );

        assertThat(exception.getCause().getMessage(), is("Mocked IOException"));
        verify(blob, never()).upload(any(InputStream.class), anyLong());
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
