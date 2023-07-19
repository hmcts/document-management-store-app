package uk.gov.hmcts.dm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, it's not a utility class
@SpringBootApplication(scanBasePackages = {
    "uk.gov.hmcts.dm",
    "uk.gov.hmcts.reform.authorisation",
})
public class DmApp {

    public static void main(String[] args) {
        SpringApplication.run(DmApp.class, args);
    }
}
