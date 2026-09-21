package uk.gov.hmcts.dm.smoke;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
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
    private List<String> allEndpoints;

    @Autowired
    MappingUrlIT(AuthTokenProvider authTokenProvider) {
        super(authTokenProvider);
    }

    @BeforeEach
    public void setup() throws JacksonException {
        JsonMapper objectMapper = JsonMapper.builder().build();
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

}
