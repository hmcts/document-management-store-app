package uk.gov.hmcts.dm.endtoend;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;
import uk.gov.hmcts.dm.security.Classifications;

import static org.hamcrest.CoreMatchers.both;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.dm.endtoend.Helper.getBinaryUrlFromResponse;
import static uk.gov.hmcts.dm.endtoend.Helper.getSelfUrlFromResponse;

@Slf4j
public class UploadDocumentTest extends End2EndTestBase {

    private HttpHeaders headers = Helper.getHeaders();

    @Test
    public void should_upload_a_document() throws Exception {
        mvc.perform(fileUpload("/documents")
            .file(FILE)
            .param("classification", Classifications.PRIVATE.toString())
            .headers(headers))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.documents[0].originalDocumentName", equalTo("test.txt")))
            .andExpect(jsonPath("$._embedded.documents[0].mimeType", equalTo("text/plain")))
            .andExpect(jsonPath("$._embedded.documents[0].createdBy", equalTo("user")))
            .andExpect(jsonPath("$._embedded.documents[0].lastModifiedBy", equalTo("user")))
            .andExpect(jsonPath("$._embedded.documents[0]._links.self.href", startsWith("http://localhost/documents/")))
            .andExpect(jsonPath("$._embedded.documents[0]._links.binary.href",
                both(startsWith("http://localhost/documents/")).and(endsWith("/binary"))));
    }

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
            .andExpect(content().bytes(FILE.getBytes()));
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
