package uk.gov.hmcts.dm.endtoend;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.security.Classifications;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.dm.endtoend.Helper.getBinaryUrlFromResponse;
import static uk.gov.hmcts.dm.endtoend.Helper.getSelfUrlFromResponse;

public class UploadDocumentTest extends FileStorageMockTest {

    public static final MockMultipartFile FILE =
        new MockMultipartFile("files", "test.txt","text/plain", "test".getBytes(StandardCharsets.UTF_8));

    @Autowired
    private MockMvc mvc;
    private HttpHeaders headers = Helper.getHeaders();

//    @Test
//    public void should_upload_a_document() throws Exception {
//        mvc.perform(fileUpload("/documents")
//            .file(FILE)
//            .param("classification", Classifications.PRIVATE.toString())
//            .headers(headers))
//            .andExpect(status().isOk())
//            .andExpect(jsonPath("$._embedded.documents[0].originalDocumentName", equalTo("test.txt")))
//            .andExpect(jsonPath("$._embedded.documents[0].mimeType", equalTo("text/plain")))
//            .andExpect(jsonPath("$._embedded.documents[0].createdBy", equalTo("user")))
//            .andExpect(jsonPath("$._embedded.documents[0].lastModifiedBy", equalTo("user")))
//            .andExpect(jsonPath("$._embedded.documents[0]._links.self.href", startsWith("http://localhost/documents/")))
//            .andExpect(jsonPath("$._embedded.documents[0]._links.binary.href",
//                both(startsWith("http://localhost/documents/")).and(endsWith("/binary"))));
//    }

    @Test
    public void should_upload_and_retrieve_a_document() throws Exception {
        final MockHttpServletResponse response = mvc.perform(fileUpload("/documents")
            .file(FILE)
            .param("classification", Classifications.PRIVATE.toString())
            .headers(headers))
            .andReturn().getResponse();

        final String url = getBinaryUrlFromResponse(response);

        mvc.perform(get(url)
            .headers(headers))
            .andExpect(status().isOk());

    }

    @Test
    public void should_upload_and_delete_a_document() throws Exception {
        final MockHttpServletResponse response = mvc.perform(fileUpload("/documents")
            .file(FILE)
            .param("classification", Classifications.PRIVATE.toString())
            .headers(headers))
            .andReturn().getResponse();

        final String url = getSelfUrlFromResponse(response);

        mvc.perform(delete(url)
                .headers(headers))
                .andExpect(status().is(204));

        mvc.perform(get(url)
                .headers(headers))
                .andExpect(status().isNotFound());
    }

    @Test
    public void should_upload_and_hard_delete_a_document() throws Exception {
        final MockHttpServletResponse response = mvc.perform(fileUpload("/documents")
                .file(FILE)
                .param("classification", Classifications.PRIVATE.toString())
                .headers(headers))
                .andReturn().getResponse();

        final String url = getSelfUrlFromResponse(response);

        mvc.perform(delete(url + "?permanent=true")
                .headers(headers))
                .andExpect(status().is(204));

        mvc.perform(get(url)
                .headers(headers))
                .andExpect(status().isNotFound());
    }

}
