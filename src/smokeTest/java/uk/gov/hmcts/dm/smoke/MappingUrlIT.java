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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@WithTags(@WithTag("testType:Smoke"))
@ExtendWith(value = {SerenityJUnit5Extension.class, SpringExtension.class})
class MappingUrlIT extends BaseIT {


    @Value("${toggle.metadatasearchendpoint}")
    private boolean metadatasearchendpoint;
    @Value("${toggle.testing}")
    private boolean testing;
    private List<String> allEndpoints;

    @Autowired
    MappingUrlIT(AuthTokenProvider authTokenProvider) {
        super(authTokenProvider);
    }

    @BeforeEach
    public void setup() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(givenUnauthenticatedRequest().get("/mappings").print());
        allEndpoints = jsonNode.findValues("predicate").stream().map(JsonNode::asText).toList();
    }

    @Test
    void toggleMetadataSearchEndpointToggleMappings() {
        assertEquals(allEndpoints.stream().anyMatch(endpoint ->
            endpoint.contains("owned")), metadatasearchendpoint);
        assertEquals(allEndpoints.stream().anyMatch(endpoint ->
            endpoint.contains("filter")), metadatasearchendpoint);
    }

    @Test
    void toggleTestingToggleTesting() {
        assertEquals(allEndpoints.stream().anyMatch(endpoint ->
            endpoint.equals("{GET [/testing/azure-storage-binary-exists/{id}]}")), testing);

    }

    public boolean getTesting() {
        return testing;
    }

    public void setTesting(boolean testing) {
        this.testing = testing;
    }

}
