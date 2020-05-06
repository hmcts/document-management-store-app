package uk.gov.hmcts.dm.endtoend;

import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;
import uk.gov.hmcts.dm.security.Classifications;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.dm.endtoend.Helper.getSelfUrlFromResponse;

public class PermissionTest extends End2EndTestBase {

    private HttpHeaders headersUser = Helper.getHeaders("user");
    private HttpHeaders headersUser2Citizen = Helper.getHeaders("user2");
    private HttpHeaders headersUserCaseWorker = Helper.getHeaders("userCaseWorker");

    @Test
    public void should_be_able_to_read_own_doc() throws Exception {
        final String url = uploadFileAndReturnSelfUrl(headersUser);

        mvc.perform(get(url)
                .headers(headersUser))
                .andExpect(status().is(200));
    }

    @Test
    public void should_be_able_to_delete_own_doc() throws Exception {
        final String url = uploadFileAndReturnSelfUrl(headersUser);

        mvc.perform(delete(url)
                .headers(headersUser))
                .andExpect(status().is(204));
    }

    @Test
    public void should_not_be_able_to_delete_another_users_doc_if_citizen() throws Exception {
        final String url = uploadFileAndReturnSelfUrl(headersUser);

        mvc.perform(delete(url)
                .headers(headersUser2Citizen))
                .andExpect(status().is(403));
    }

    @Test
    public void should_not_be_able_to_delete_another_users_doc_if_caseworker() throws Exception {
        final String url = uploadFileAndReturnSelfUrl(headersUser);

        mvc.perform(delete(url)
                .headers(headersUserCaseWorker))
                .andExpect(status().is(403));
    }

    @Test
    public void should_be_able_to_read_another_users_doc_if_a_caseworker() throws Exception {
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

        return getSelfUrlFromResponse(response);
    }

}
