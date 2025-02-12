package uk.gov.hmcts.dm.endtoend;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.DmApp;
import uk.gov.hmcts.dm.controller.testing.TestController;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.service.BlobStorageDeleteService;
import uk.gov.hmcts.dm.service.BlobStorageReadService;
import uk.gov.hmcts.dm.service.BlobStorageWriteService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = DmApp.class)
@AutoConfigureMockMvc
@WithMockUser
@ActiveProfiles("local")
@TestPropertySource(locations = "classpath:application-local.yaml")
public abstract class End2EndTestBase {

    protected static final MockMultipartFile FILE =
        new MockMultipartFile("files", "test.txt","text/plain", "test".getBytes(StandardCharsets.UTF_8));

    protected MockMvc mvc;

    @Autowired
    protected WebApplicationContext webApplicationContext;

    @MockitoBean
    protected BlobStorageWriteService blobStorageWriteService;

    @MockitoBean
    protected BlobStorageReadService blobStorageReadService;

    @MockitoBean
    protected BlobStorageDeleteService blobStorageDeleteService;

    @MockitoBean
    protected TestController testController;


    @BeforeEach
    public void setUp() throws IOException {
        this.mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply(springSecurity())
            .build();

        doAnswer(invocation -> {
            HttpServletResponse r = invocation.getArgument(2);
            try (final InputStream inputStream = FILE.getInputStream();
                 final OutputStream out = r.getOutputStream()
            ) {
                IOUtils.copy(inputStream, out);
                return null;
            }
        }).when(blobStorageReadService).loadBlob(
            any(DocumentContentVersion.class),
            any(HttpServletRequest.class),
            any(HttpServletResponse.class)
        );

        doAnswer(invocation -> {
            try (final InputStream inputStream = FILE.getInputStream();
                 final OutputStream out = invocation.getArgument(1)
            ) {
                IOUtils.copy(inputStream, out);
                return null;
            }
        }).when(blobStorageReadService).loadFullBlob(any(DocumentContentVersion.class), any(OutputStream.class));

        doAnswer(invocation -> {
            uploadDocument(invocation);
            return null;
        }).when(blobStorageWriteService)
            .uploadDocumentContentVersion(any(StoredDocument.class),
                                          any(DocumentContentVersion.class),
                                          any(MultipartFile.class));
    }

    private void uploadDocument(final InvocationOnMock invocation) {
        final DocumentContentVersion dcv = invocation.getArgument(1);
        dcv.setContentUri("uri");
        dcv.setContentChecksum("checksum");
    }
}
