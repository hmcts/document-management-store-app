package uk.gov.hmcts.dm.service;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.hmcts.dm.componenttests.TestUtil;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.exception.CantReadDocumentContentVersionBinaryException;

import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CloudBlobContainer.class, CloudBlockBlob.class})
public class BlobStorageReadServiceTest {

    static final Logger LOGGER = LoggerFactory.getLogger(BlobStorageReadServiceTest.class);

    private BlobStorageReadService blobStorageReadService;

    private CloudBlockBlob blob;
    private CloudBlobContainer cloudBlobContainer;
    private DocumentContentVersion documentContentVersion;
    private OutputStream outputStream;

    @Before
    public void setUp() {
        cloudBlobContainer = PowerMockito.mock(CloudBlobContainer.class);
        blob = PowerMockito.mock(CloudBlockBlob.class);
        outputStream = mock(OutputStream.class);
        documentContentVersion = TestUtil.DOCUMENT_CONTENT_VERSION;
        blobStorageReadService = new BlobStorageReadService(cloudBlobContainer);
    }

    @Test
    public void loadsBlob() throws URISyntaxException, StorageException {
        given(cloudBlobContainer.getBlockBlobReference(documentContentVersion.getId().toString())).willReturn(blob);

        blobStorageReadService.loadBlob(documentContentVersion, outputStream);

        verify(blob).download(outputStream);
    }

    @Test(expected = CantReadDocumentContentVersionBinaryException.class)
    public void throwsCantReadDocumentContentVersionBinaryException() throws URISyntaxException, StorageException {
        given(cloudBlobContainer.getBlockBlobReference(documentContentVersion.getId()
            .toString())).willThrow(new StorageException("404",
            "Message", mock(Exception.class)));

        blobStorageReadService.loadBlob(documentContentVersion, outputStream);
    }

    @AfterClass
    public static void killRuntimeAgainstNonDaemonThreadsNotResponding() {
        
        // Initiate a new Thread that will wait 2 minutes for test executions to finish,
        // and will kill the JVM on which the test suite is being executed.
        // This trick is needed against the none-daemon threads left after test executions,
        // which are not responding to any signals, causing test JVM and gradle to hang forever.
        new Thread() {
            public void run() {
                try {
                    TimeUnit.SECONDS.sleep(120);
                } catch (InterruptedException e) {
                    LOGGER.error("Exception trying to forcefully halt the test suit execution.", e);
                }
                Runtime.getRuntime().halt(0);            
            }
        }.start();
    }
}
