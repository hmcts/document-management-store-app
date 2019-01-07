package uk.gov.hmcts.dm.service;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
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
import static org.apache.tika.io.IOUtils.toInputStream;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CloudBlobContainer.class, CloudBlockBlob.class})
public class BlobStorageWriteServiceTest {

    private BlobStorageWriteService blobStorageWriteService;

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
        blobStorageWriteService = new BlobStorageWriteService(cloudBlobContainer, documentContentVersionRepository);
        try (final InputStream inputStream = toInputStream(MOCK_DATA)) {
            given(file.getInputStream()).willReturn(inputStream);
        }
    }

    @Test
    public void uploadDocumentContentVersion() throws Exception {
        final StoredDocument storedDocument = createStoredDocument();
        final DocumentContentVersion documentContentVersion = storedDocument.getDocumentContentVersions().get(0);
        given(cloudBlobContainer.getBlockBlobReference(documentContentVersion.getId().toString())).willReturn(blob);
        String azureProvidedUri = "someuri";
        given(blob.getUri()).willReturn(new URI(azureProvidedUri));
        doAnswer(invocation -> {
            try (final InputStream inputStream = toInputStream(MOCK_DATA);
                 final OutputStream outputStream = invocation.getArgumentAt(0, OutputStream.class)
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

    @Test(expected = FileStorageException.class)
    public void uploadDocumentContentVersionChecksumFailed() throws Exception {
        final StoredDocument storedDocument = createStoredDocument();
        final DocumentContentVersion documentContentVersion = storedDocument.getDocumentContentVersions().get(0);
        given(cloudBlobContainer.getBlockBlobReference(documentContentVersion.getId().toString())).willReturn(blob);
        String azureProvidedUri = "someuri";
        given(blob.getUri()).willReturn(new URI(azureProvidedUri));

        // upload
        blobStorageWriteService.uploadDocumentContentVersion(storedDocument,
                                                             documentContentVersion,
                                                             file);

        assertThat(documentContentVersion.getContentUri(), is(azureProvidedUri));
        verify(blob).upload(file.getInputStream(), documentContentVersion.getSize());
    }

    @Test(expected = FileStorageException.class)
    public void uploadDocumentContentVersionThrowsFileStorageException() throws Exception {
        given(cloudBlobContainer.getBlockBlobReference(anyString())).willThrow(new StorageException("Bad",
                                                                                                    "Things happened",
                                                                                                    new IOException()));
        final StoredDocument storedDocument = createStoredDocument();
        final DocumentContentVersion documentContentVersion = storedDocument.getDocumentContentVersions().get(0);

        blobStorageWriteService.uploadDocumentContentVersion(storedDocument,
                                                             documentContentVersion,
                                                             file);
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
