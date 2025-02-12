package uk.gov.hmcts.dm.smoke;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.serenitybdd.annotations.WithTag;
import net.serenitybdd.annotations.WithTags;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@WithTags(@WithTag("testType:Smoke"))
@ExtendWith(value = {SerenityJUnit5Extension.class, SpringExtension.class})
class MappingUrlIT extends BaseIT {


    @Value("${toggle.metadatasearchendpoint}")
    private boolean metadatasearchendpoint;
    @Value("${toggle.documentandmetadatauploadendpoint}")
    private boolean documentandmetadatauploadendpoint;
    @Value("${toggle.ttl}")
    private boolean ttl;
    @Value("${toggle.testing}")
    private boolean testing;
    private List<String> allEndpoints;

    @BeforeEach
    public void setup() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(givenUnauthenticatedRequest().get("/mappings").print());
        allEndpoints = jsonNode.findValues("predicate").stream().map(JsonNode::asText).toList();
    }

    @Test
    void toggle_metadatasearchendpoint_toggle_Mappings() {
        assertEquals(allEndpoints.stream().anyMatch(endpoint ->
            endpoint.contains("owned")), metadatasearchendpoint);
        assertEquals(allEndpoints.stream().anyMatch(endpoint ->
            endpoint.contains("filter")), metadatasearchendpoint);
    }

    @Test
    void toggle_testing_toggle_Testing() {
        assertEquals(allEndpoints.stream().anyMatch(endpoint ->
            endpoint.equals("{GET [/testing/azure-storage-binary-exists/{id}]}")), testing);

    }

    public boolean getMetadatasearchendpoint() {
        return metadatasearchendpoint;
    }

    public boolean isMetadatasearchendpoint() {
        return metadatasearchendpoint;
    }

    public void setMetadatasearchendpoint(boolean metadatasearchendpoint) {
        this.metadatasearchendpoint = metadatasearchendpoint;
    }

    public boolean getDocumentandmetadatauploadendpoint() {
        return documentandmetadatauploadendpoint;
    }

    public boolean isDocumentandmetadatauploadendpoint() {
        return documentandmetadatauploadendpoint;
    }

    public void setDocumentandmetadatauploadendpoint(boolean documentandmetadatauploadendpoint) {
        this.documentandmetadatauploadendpoint = documentandmetadatauploadendpoint;
    }

    public boolean getTtl() {
        return ttl;
    }

    public boolean isTtl() {
        return ttl;
    }

    public void setTtl(boolean ttl) {
        this.ttl = ttl;
    }

    public boolean getTesting() {
        return testing;
    }

    public boolean isTesting() {
        return testing;
    }

    public void setTesting(boolean testing) {
        this.testing = testing;
    }

    public List getAllEndpoints() {
        return allEndpoints;
    }

    public void setAllEndpoints(List allEndpoints) {
        this.allEndpoints = allEndpoints;
    }

}
