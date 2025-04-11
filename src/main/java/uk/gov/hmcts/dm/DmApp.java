package uk.gov.hmcts.dm;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import uk.gov.hmcts.dm.service.ScheduledTaskRunner;

import java.util.Objects;

@EnableScheduling
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, it's not a utility class
@SpringBootApplication(scanBasePackages = {
    "uk.gov.hmcts.dm",
    "uk.gov.hmcts.reform.authorisation",
})
public class DmApp implements CommandLineRunner {

    private static final String TASK_NAME = "TASK_NAME";


    private final ScheduledTaskRunner taskRunner;

    public DmApp(ScheduledTaskRunner taskRunner) {
        this.taskRunner = taskRunner;
    }

    public static void main(String[] args) {
        final var application = new SpringApplication(DmApp.class);
        final var instance = application.run(args);

        //When TASK_NAME exists, we need the Application to be run as AKS job.
        if (Objects.nonNull(System.getenv(TASK_NAME))) {
            instance.close();
        }
    }

    @Override
    public void run(String... args) {
        if (Objects.nonNull(System.getenv(TASK_NAME))) {
            taskRunner.run(System.getenv(TASK_NAME));
        }
    }
}
