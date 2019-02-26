package uk.gov.hmcts.dm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@EnableSwagger2
@ComponentScan("uk.gov.hmcts.dm.controller")
public class SwaggerConfiguration {

    @Value("${api.version}")
    private String apiVersion;

    private static final String MODEL_REF_TYPE = "string";
    private static final String PARAMETER_TYPE = "header";

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.regex("/documents(.*)"))
                .build()
                .globalOperationParameters(
                    Stream.of(new ParameterBuilder()
                            .name("ServiceAuthorization")
                            .description("Service Auth (S2S). Use it when accessing the API on App Tier level.")
                            .modelRef(new ModelRef(MODEL_REF_TYPE))
                            .parameterType(PARAMETER_TYPE)
                            .required(true)
                            .build()).collect(Collectors.toList()))
                .globalOperationParameters(
                    Stream.of(new ParameterBuilder()
                        .name("user-id")
                        .description("User-id of the currently authenticated user. If provided will be used to populate the creator field of a document and will be used for authorisation.")
                        .modelRef(new ModelRef(MODEL_REF_TYPE))
                        .parameterType(PARAMETER_TYPE)
                        .required(false)
                        .build()).collect(Collectors.toList()))
                .globalOperationParameters(
                    Stream.of(new ParameterBuilder()
                        .name("user-roles")
                        .description("Comma-separated list of roles of the currently authenticated user. If provided will be used for authorisation.")
                        .modelRef(new ModelRef(MODEL_REF_TYPE))
                        .parameterType(PARAMETER_TYPE)
                        .required(false)
                        .build()).collect(Collectors.toList()))
            .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Document Management API")
                .description("Documented API for the interim document management solution."
                    + "To use the API calls generate an Authorization JWT Tokens (user and service) which is required in the header.")
                .version(apiVersion)
                .build();
    }

}
