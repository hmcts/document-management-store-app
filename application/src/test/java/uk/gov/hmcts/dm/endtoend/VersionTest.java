package uk.gov.hmcts.dm.endtoend;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.dm.DmApp;
import uk.gov.hmcts.dm.config.azure.AzureStorageConfiguration;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.security.Classifications;
import uk.gov.hmcts.dm.service.BlobStorageReadService;
import uk.gov.hmcts.dm.service.BlobStorageWriteService;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.dm.endtoend.Helper.getSelfUrlFromResponse;

@RunWith(SpringRunner.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = DmApp.class)
@AutoConfigureMockMvc
@ActiveProfiles("local")
@TestPropertySource(locations = "classpath:application-local.yaml")
public class VersionTest {

    public static final MockMultipartFile FILE_V1 =
        new MockMultipartFile("files", "test.txt","text/plain", "test".getBytes(StandardCharsets.UTF_8));

    public static final MockMultipartFile FILE_V2 =
        new MockMultipartFile("file", "test.txt","text/plain", "test".getBytes(StandardCharsets.UTF_8));

    @Autowired
    protected MockMvc mvc;

    @MockBean
    protected AzureStorageConfiguration azureStorageConfiguration;

    @MockBean
    protected BlobStorageWriteService blobStorageWriteService;

    @MockBean
    protected BlobStorageReadService blobStorageReadService;

    private HttpHeaders headers = Helper.getHeaders();

    private static MockHttpServletRequest setMethodToPost(MockHttpServletRequest request) {
        request.setMethod("POST");
        return request;
    }

    @Test
    public void should_upload_a_second_version_of_a_document() throws Exception {
        mockReadFromAzureBlobStorage();

        final MockHttpServletResponse response = mvc.perform(fileUpload("/documents")
                .file(FILE_V1)
                .param("classification", Classifications.PRIVATE.toString())
                .headers(headers))
                .andReturn().getResponse();

        final String url = getSelfUrlFromResponse(response);

        mvc.perform(fileUpload(url)
                .file(FILE_V2)
                .with(VersionTest::setMethodToPost)
                .headers(headers))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$._links.length()", is(5)));
    }

    private void mockReadFromAzureBlobStorage() {
        doAnswer(invocation -> {
            final OutputStream out = invocation.getArgumentAt(1, OutputStream.class);
            IOUtils.copy(FILE_V1.getInputStream(), out);
            out.close();
            return null;
        }).doAnswer(invocation -> {
            final OutputStream out = invocation.getArgumentAt(1, OutputStream.class);
            IOUtils.copy(FILE_V2.getInputStream(), out);
            out.close();
            return null;
        })
            .when(blobStorageReadService)
            .loadBlob(Mockito.any(DocumentContentVersion.class), Mockito.any(OutputStream.class));
    }
}
