package uk.gov.hmcts.dm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


@SpringBootApplication
@EnableSwagger2
@EnableAutoConfiguration
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class DmApp {

    public static void main(String[] args) {
        SpringApplication.run(DmApp.class, args);
    }

}
