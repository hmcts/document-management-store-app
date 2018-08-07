package uk.gov.hmcts.dm.endtoend;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.dm.security.Classifications;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.dm.endtoend.Helper.getSelfUrlFromResponse;


public class SearchDocumentTest extends FileStorageMockTest {

    public static final MockMultipartFile FILE =
        new MockMultipartFile("files", "test.txt","text/plain", "test".getBytes(StandardCharsets.UTF_8));

    @Autowired
    private MockMvc mvc;
    private final HttpHeaders headers = Helper.getHeaders();

    @Test
    public void deleted_doc_should_not_appear_in_search() throws Exception {
        final MockHttpServletResponse response = mvc.perform(fileUpload("/documents")
            .file(FILE)
            .param("classification", Classifications.PRIVATE.toString())
            .headers(headers))
            .andReturn().getResponse();

        final String url = getSelfUrlFromResponse(response);

//        mvc.perform(post("/documents/owned?size=5&sort=createdOn,desc")
//            .headers(headers))
//            .andExpect(content().string(CoreMatchers.containsString(url)));

        mvc.perform(delete(url)
            .headers(headers))
            .andExpect(status().is(204));

//        mvc.perform(post("/documents/owned?size=5&sort=createdOn,desc")
//            .headers(headers))
//            .andExpect(content().string(CoreMatchers.not(CoreMatchers.containsString(url))));
    }

}
