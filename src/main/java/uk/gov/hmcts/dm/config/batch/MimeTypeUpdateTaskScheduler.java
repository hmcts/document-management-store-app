package uk.gov.hmcts.dm.config.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Schedules the MimeTypeUpdateTask to run at a fixed rate.
 * This scheduler can be enabled or disabled via configuration properties.
 */
@Component
public class MimeTypeUpdateTaskScheduler {

    private static final Logger log = LoggerFactory.getLogger(MimeTypeUpdateTaskScheduler.class);

    private final MimeTypeUpdateTask mimeTypeUpdateTask;

    public MimeTypeUpdateTaskScheduler(MimeTypeUpdateTask mimeTypeUpdateTask) {
        this.mimeTypeUpdateTask = mimeTypeUpdateTask;
        log.info("MimeTypeUpdateTaskScheduler enabled.");
    }

    /**
     * Runs the MimeTypeUpdateTask at a fixed rate defined in the application properties.
     * The default rate is every 30 seconds.
     */
    @Scheduled(fixedRateString = "3000}")
    public void runMimeTypeUpdateTask() {
        log.info("Triggering MimeTypeUpdateTask...");
        mimeTypeUpdateTask.run();
    }
}
