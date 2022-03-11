package uk.gov.hmcts.dm.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration {

    public OpenAPI api() {
        return new OpenAPI()
            .info(
                new Info().title("Document Management Store API")
                    .description("API to upload and download Documents, retrieve metadata associated with the Documents.")
                    .version("2-beta")
            );
    }
}
