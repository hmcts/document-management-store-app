package uk.gov.hmcts.dm.endtoend;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.DmApp;
import uk.gov.hmcts.dm.config.azure.AzureStorageConfiguration;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.service.BlobStorageReadService;
import uk.gov.hmcts.dm.service.BlobStorageWriteService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = DmApp.class)
@AutoConfigureMockMvc
@ActiveProfiles("local")
@TestPropertySource(locations = "classpath:application-local.yaml")
public abstract class End2EndTestBase {

    protected static final MockMultipartFile FILE =
        new MockMultipartFile("files", "test.txt","text/plain", "test".getBytes(StandardCharsets.UTF_8));

    @Autowired
    protected MockMvc mvc;

    @MockBean
    protected AzureStorageConfiguration azureStorageConfiguration;

    @MockBean
    protected BlobStorageWriteService blobStorageWriteService;

    @MockBean
    protected BlobStorageReadService blobStorageReadService;

    @Before
    public void setUp() {
        when(azureStorageConfiguration.isAzureBlobStoreEnabled()).thenReturn(true);
        when(azureStorageConfiguration.isPostgresBlobStorageEnabled()).thenReturn(false);

        doAnswer(invocation -> {
            try (final InputStream inputStream = FILE.getInputStream();
                 final OutputStream out = invocation.getArgumentAt(1, OutputStream.class)
            ) {
                IOUtils.copy(inputStream, out);
                return null;
            }
        }).when(blobStorageReadService).loadBlob(any(DocumentContentVersion.class), any(OutputStream.class));
        doAnswer(invocation -> {
            uploadDocument(invocation);
            return null;
        }).when(blobStorageWriteService)
            .uploadDocumentContentVersion(any(StoredDocument.class),
                                          any(DocumentContentVersion.class),
                                          any(MultipartFile.class));
    }

    private void uploadDocument(final InvocationOnMock invocation) throws IOException {
        final DocumentContentVersion dcv = invocation.getArgumentAt(1, DocumentContentVersion.class);
        dcv.setContentUri("uri");
        dcv.setContentChecksum("checksum");
    }
}
