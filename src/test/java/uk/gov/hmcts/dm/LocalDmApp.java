package uk.gov.hmcts.dm;

import org.springframework.boot.builder.SpringApplicationBuilder;

public class LocalDmApp {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
            .sources(DmApp.class)
            .profiles("local")
            .run();
    }

}
