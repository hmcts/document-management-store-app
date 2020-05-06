package uk.gov.hmcts.dm;

import org.springframework.boot.builder.SpringApplicationBuilder;

@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class LocalDmApp {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(DmApp.class)
                .profiles("local")
                .run();
    }

}
