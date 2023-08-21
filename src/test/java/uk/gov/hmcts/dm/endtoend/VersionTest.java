package uk.gov.hmcts.dm.endtoend;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.security.Classifications;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.dm.endtoend.Helper.getSelfUrlFromResponse;

public class VersionTest extends End2EndTestBase {

    private static final MockMultipartFile FILE_V1 =
        new MockMultipartFile("files", "test.txt","text/plain", "test".getBytes(StandardCharsets.UTF_8));

    private static final MockMultipartFile FILE_V2 =
        new MockMultipartFile("file", "test.txt","text/plain", "test".getBytes(StandardCharsets.UTF_8));

    private HttpHeaders headers = Helper.getHeaders();

    private static MockHttpServletRequest setMethodToPost(MockHttpServletRequest request) {
        request.setMethod("POST");
        return request;
    }

    @Override
    @Before
    public void setUp() throws IOException {
        super.setUp();
        doAnswer(invocation -> {
            try (final InputStream inputStream = FILE_V1.getInputStream();
                 final OutputStream out = invocation.getArgument(1)
            ) {
                IOUtils.copy(inputStream, out);
                return null;
            }
        }).doAnswer(invocation -> {
            try (final InputStream inputStream = FILE_V2.getInputStream();
                 final OutputStream out = invocation.getArgument(1)
            ) {
                IOUtils.copy(inputStream, out);
                return null;
            }
        })
            .when(blobStorageReadService)
            .loadBlob(
                Mockito.any(DocumentContentVersion.class),
                Mockito.any(HttpServletRequest.class),
                Mockito.any(HttpServletResponse.class));
    }

    @Test
    public void should_upload_a_second_version_of_a_document() throws Exception {

        final MockHttpServletResponse response = mvc.perform(multipart("/documents")
                .file(FILE_V1)
                .param("classification", Classifications.PRIVATE.toString())
                .headers(headers))
                .andReturn().getResponse();

        final String url = getSelfUrlFromResponse(response);

        mvc.perform(multipart(url)
                .file(FILE_V2)
                .with(VersionTest::setMethodToPost)
                .headers(headers))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$._links.length()", is(4)));
    }
}
