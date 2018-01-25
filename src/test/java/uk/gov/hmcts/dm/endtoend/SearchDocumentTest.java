package uk.gov.hmcts.dm.endtoend;

import org.hamcrest.CoreMatchers;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.dm.endtoend.Helper.getSelfUrlFromResponse;

@RunWith(SpringRunner.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = DmApp.class)
@AutoConfigureMockMvc
@ActiveProfiles("local")
@TestPropertySource(
    locations = "classpath:application-local.yaml")
public class SearchDocumentTest {

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

        mvc.perform(post("/documents/owned?size=5&sort=createdOn,desc")
            .headers(headers))
            .andExpect(content().string(CoreMatchers.containsString(url)));

        mvc.perform(delete(url)
            .headers(headers))
            .andExpect(status().is(204));

        mvc.perform(post("/documents/owned?size=5&sort=createdOn,desc")
            .headers(headers))
            .andExpect(content().string(CoreMatchers.not(CoreMatchers.containsString(url))));
    }

}
