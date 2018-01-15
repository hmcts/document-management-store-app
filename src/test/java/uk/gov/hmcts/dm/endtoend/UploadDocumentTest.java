package uk.gov.hmcts.dm.endtoend;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.dm.DmApp;
import uk.gov.hmcts.dm.security.Classifications;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uk.gov.hmcts.dm.endtoend.Helper.getBinaryUrlFromResponse;
import static uk.gov.hmcts.dm.endtoend.Helper.getSelfUrlFromResponse;

@RunWith(SpringRunner.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = DmApp.class)
@AutoConfigureMockMvc
@ActiveProfiles("local")
@TestPropertySource(
    locations = "classpath:application-local.yaml")
public class UploadDocumentTest {

    public static final MockMultipartFile FILE =
        new MockMultipartFile("files", "test.txt","text/plain", "test".getBytes(StandardCharsets.UTF_8));

    @Autowired
    private MockMvc mvc;
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
                .andExpect(status().is(410));

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

        mvc.perform(delete(url + "/removePermanently")
                .headers(headers))
                .andExpect(status().is(410));

        mvc.perform(get(url)
                .headers(headers))
                .andExpect(status().isNotFound());
    }

}
