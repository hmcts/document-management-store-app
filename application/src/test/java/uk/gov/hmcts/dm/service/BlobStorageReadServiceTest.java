package uk.gov.hmcts.dm.service;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.exception.CantReadDocumentContentVersionBinaryException;

import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest( {CloudBlobContainer.class, CloudBlockBlob.class})
public class BlobStorageReadServiceTest {

    private BlobStorageReadService blobStorageReadService;

    private CloudBlockBlob blob;
    private CloudBlobContainer cloudBlobContainer;
    private UUID uuid = UUID.randomUUID();
    private DocumentContentVersion documentContentVersion;
    private OutputStream outputStream;

    @Before
    public void setUp() {
        cloudBlobContainer = PowerMockito.mock(CloudBlobContainer.class);
        blob = PowerMockito.mock(CloudBlockBlob.class);
        outputStream = Mockito.mock(OutputStream.class);
        documentContentVersion = buildDocument(false, uuid);
        blobStorageReadService = new BlobStorageReadService(cloudBlobContainer);
    }

    @Test
    public void loadsBlob() throws URISyntaxException, StorageException {
        given(cloudBlobContainer.getBlockBlobReference(uuid.toString())).willReturn(blob);

        blobStorageReadService.loadBlob(documentContentVersion, outputStream);

        verify(blob).download(outputStream);
    }

    @Test(expected = CantReadDocumentContentVersionBinaryException.class)
    public void throwsCantReadDocumentContentVersionBinaryException() throws URISyntaxException, StorageException {
        given(cloudBlobContainer.getBlockBlobReference(uuid.toString())).willThrow(new StorageException("404",
            "Message", mock(Exception.class)));

        blobStorageReadService.loadBlob(documentContentVersion, outputStream);
    }

    private DocumentContentVersion buildDocument(boolean deleted, UUID documentContentVersionUuid) {
        DocumentContentVersion doc = new DocumentContentVersion();
        doc.setId(documentContentVersionUuid);
        doc.setStoredDocument(createStoredDocument(deleted));
        doc.setSize(1L);
        return doc;
    }

    private StoredDocument createStoredDocument(boolean deleted) {
        StoredDocument storedDoc = new StoredDocument();
        storedDoc.setDeleted(deleted);
        return storedDoc;
    }
}
