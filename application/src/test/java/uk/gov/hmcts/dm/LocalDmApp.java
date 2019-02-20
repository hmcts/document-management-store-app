package uk.gov.hmcts.dm;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication(
	    scanBasePackages = "uk.gov.hmcts"
	)
@EnableSwagger2
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class LocalDmApp {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(DmApp.class)
                .profiles("local")
                .run();
    }

}
