package uk.gov.hmcts.dm.endtoend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.dm.security.Classifications;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.dm.endtoend.Helper.getSelfUrlFromResponse;

public class AuditTest extends End2EndTestBase {

    private HttpHeaders headers = Helper.getHeaders();

    @Test
    public void should_audit_upload_a_document() throws Exception {
        final String url = uploadFileAndReturnSelfUrl();

        final MvcResult auditResponse = mvc.perform(get(url + "/auditEntries")
                .headers(headers))
                .andExpect(status().isOk())
                .andReturn();

        final JsonNode auditEntries = getAuditEntriesFromResponse(auditResponse);

        assertThat(auditEntries.size(), is(2));
        assertThat(auditEntries.get(0).get("action").asText(), equalTo("CREATED"));
        assertThat(auditEntries.get(1).get("action").asText(), equalTo("CREATED"));
    }

    @Test
    public void should_audit_retrieval() throws Exception {
        final String url = uploadFileAndReturnSelfUrl();

        mvc.perform(get(url)
                .headers(headers));

        final MvcResult auditResponse = mvc.perform(get(url + "/auditEntries")
                .headers(headers))
                .andExpect(status().isOk())
                .andReturn();

        final JsonNode auditEntries = getAuditEntriesFromResponse(auditResponse);

        assertThat(auditEntries.size(), is(3));
        assertThat(auditEntries.get(2).get("action").asText(), equalTo("READ"));
    }

    @Test
    public void should_audit_delete() throws Exception {
        final String url = uploadFileAndReturnSelfUrl();

        mvc.perform(delete(url)
                .headers(headers));

        final MvcResult auditResponse = mvc.perform(get(url + "/auditEntries")
                .headers(headers))
                .andExpect(status().isOk())
                .andReturn();

        final JsonNode auditEntries = getAuditEntriesFromResponse(auditResponse);

        assertThat(auditEntries.size(), is(3));
        assertThat(auditEntries.get(2).get("action").asText(), equalTo("DELETED"));
    }

    private JsonNode getAuditEntriesFromResponse(MvcResult auditResponse) throws IOException {
        return new ObjectMapper().readTree(auditResponse.getResponse().getContentAsString())
                .at("/_embedded/auditEntries");
    }

    private String uploadFileAndReturnSelfUrl() throws Exception {
        final MockHttpServletResponse response = mvc.perform(fileUpload("/documents")
                .file(FILE)
                .param("classification", Classifications.PRIVATE.toString())
                .headers(headers))
                .andReturn().getResponse();

        return getSelfUrlFromResponse(response);
    }

}
