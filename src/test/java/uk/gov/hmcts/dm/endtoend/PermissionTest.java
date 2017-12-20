package uk.gov.hmcts.dm.endtoend;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.dm.DmApp;
import uk.gov.hmcts.dm.security.Classifications;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = DmApp.class)
@AutoConfigureMockMvc
@ActiveProfiles("local")
public class PermissionTest {

    public static final MockMultipartFile FILE =
            new MockMultipartFile("files", "test.txt","text/plain", "test".getBytes());

    @Autowired
    private MockMvc mvc;
    private HttpHeaders headersUser = Helper.getHeaders("user");
    private HttpHeaders headersUser2Citizen = Helper.getHeaders("user2");
    private HttpHeaders headersUserCaseWorker = Helper.getHeaders("userCaseWorker");

    @Test
    public void shouldBeAbleToReadOwnDoc() throws Exception {
        final String url = uploadFileAndReturnSelfUrl(headersUser);

        mvc.perform(get(url)
                .headers(headersUser))
                .andExpect(status().is(200));
    }

    @Test
    @Ignore("not currently implemented")
    public void shouldBeAbleToDeleteOwnDoc() throws Exception {
        final String url = uploadFileAndReturnSelfUrl(headersUser);

        mvc.perform(delete(url)
                .headers(headersUser))
                .andExpect(status().is(204));
    }

    @Test
    @Ignore("not currently implemented")
    public void shouldNotBeAbleToDeleteAnotherUsersDocIfCitizen() throws Exception {
        final String url = uploadFileAndReturnSelfUrl(headersUser);

        mvc.perform(delete(url)
                .headers(headersUser2Citizen))
                .andExpect(status().is(403));
    }

    @Test
    @Ignore("not currently implemented")
    public void shouldNotBeAbleToDeleteAnotherUsersDocIfCaseworker() throws Exception {
        final String url = uploadFileAndReturnSelfUrl(headersUser);

        mvc.perform(delete(url)
                .headers(headersUserCaseWorker))
                .andExpect(status().is(403));
    }

    @Test
    public void shouldBeAbleToReadAnotherUsersDocIfACaseworker() throws Exception {
        final String url = uploadFileAndReturnSelfUrl(headersUser);

        mvc.perform(get(url)
                .headers(headersUserCaseWorker))
                .andExpect(status().is(200));
    }

    private String uploadFileAndReturnSelfUrl(HttpHeaders headers) throws Exception {
        final MockHttpServletResponse response = mvc.perform(fileUpload("/documents")
                .file(FILE)
                .param("classification", Classifications.PRIVATE.toString())
                .headers(headers))
                .andReturn().getResponse();

        return Helper.getSelfUrlFromResponse(response);
    }

}
