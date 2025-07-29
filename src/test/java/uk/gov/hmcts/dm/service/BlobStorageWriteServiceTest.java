package uk.gov.hmcts.dm.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.options.BlockBlobOutputStreamOptions;
import com.azure.storage.blob.specialized.BlobOutputStream;
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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    void setUp() throws Exception {

        given(cloudBlobContainer.getBlobClient(any())).willReturn(blobClient);
        given(blobClient.getBlockBlobClient()).willReturn(blob);

        blobStorageWriteService = new BlobStorageWriteService(cloudBlobContainer, documentContentVersionRepository);

        byte[] fakeBytes = "test content".getBytes();
        when(file.getBytes()).thenReturn(fakeBytes);
    }

    @Test
    void uploadDocumentContentVersion() throws Exception {
        final StoredDocument storedDocument = createStoredDocument();
        final DocumentContentVersion documentContentVersion = storedDocument.getDocumentContentVersions().get(0);
        String azureProvidedUri = "someuri";
        given(blob.getBlobUrl()).willReturn(new URI(azureProvidedUri).toString());

        doAnswer(invocation -> {
            final OutputStream outputStream = invocation.getArgument(0);
            outputStream.write(MOCK_DATA.getBytes(UTF8));
            return MOCK_DATA.getBytes(UTF8).length;
        }).when(blob).downloadStream(any(OutputStream.class));

        BlobOutputStream mockBlobOutputStream = mock(BlobOutputStream.class);
        when(blob.getBlobOutputStream(any(BlockBlobOutputStreamOptions.class))).thenReturn(mockBlobOutputStream);

        doNothing().when(mockBlobOutputStream).write(any(byte[].class));
        doNothing().when(mockBlobOutputStream).close();

        // upload
        blobStorageWriteService.uploadDocumentContentVersion(storedDocument,
            documentContentVersion,
            file);

        assertThat(documentContentVersion.getContentUri(), is(azureProvidedUri));
        verify(blob).getBlobOutputStream(any(BlockBlobOutputStreamOptions.class));
        verify(mockBlobOutputStream).write(any(byte[].class));
        verify(mockBlobOutputStream).close();
        verify(blob, never()).upload(any(), anyLong());
    }

    @Test
    void writeBinaryStreamThrowsFileStorageExceptionOnIOException() throws Exception {
        final StoredDocument storedDocument = createStoredDocument();
        final DocumentContentVersion documentContentVersion = storedDocument.getDocumentContentVersions().get(0);
        given(file.getBytes()).willThrow(new IOException("Mocked IOException"));

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
