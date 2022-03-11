package uk.gov.hmcts.dm.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration {

    public OpenAPI api() {
        return new OpenAPI()
            .info(
                new Info().title("Document Assembly API")
                    .description("download, upload")
                    .version("2-beta")
            );
    }
}
