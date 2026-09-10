package uk.gov.hmcts.dm.openapi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.dm.componenttests.ComponentTestBase;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
class OpenAPIPublisherTest extends ComponentTestBase {

    private final MockMvc mockMvc;

    @Autowired
    OpenAPIPublisherTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @DisplayName("Generate swagger documentation")
    @Test
    void generateDocs() throws Exception {
        byte[] specs = mockMvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsByteArray();

        Path outputPath = Path.of(System.getProperty("java.io.tmpdir"), "openapi-specs.json");
        try (OutputStream outputStream = Files.newOutputStream(outputPath)) {
            outputStream.write(specs);
        }
    }
}
