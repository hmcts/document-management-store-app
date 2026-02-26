package uk.gov.hmcts.dm;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import uk.gov.hmcts.dm.service.DocumentDeletionService;

import java.time.LocalDate;

@SpringBootApplication(scanBasePackages = {"uk.gov.hmcts.dm","uk.gov.hmcts.reform.authorisation"})
@EnableScheduling
public class DocumentDeletionApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentDeletionService.class);

    private final DocumentDeletionService deletionService;

    // optional ISO date (e.g. 2025-08-01). If blank, falls back to 1 year ago.
    @Value("${deletion.cutoffDate:2025-09-01}")
    private String cutoffDateProp;

    public DocumentDeletionApplication(DocumentDeletionService deletionService) {
        this.deletionService = deletionService;
    }

    public static void main(String[] args) {
        SpringApplication.run(DocumentDeletionApplication.class, args);
    }

    @Scheduled(cron = "${deletion.cron:0 0/5 * * * ?}")
    public void scheduledDeletion() {
        LocalDate cutoff = parseOrDefaultCutoff(cutoffDateProp);
        LOGGER.info("Starting batched deletion with cutoffDate = {} ", cutoff);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            deletionService.runBatchedDeletion(cutoff);
            LOGGER.info("Batched deletion data prep completed for cutoffDate = {} ", cutoff);
        } catch (Exception e) {
            LOGGER.error("Batched deletion data prep failed for cutoffDate={}", cutoff, e);
            throw e;
        }
        stopWatch.stop();
        LOGGER.info("Batched deletion data prep job took {} ms", stopWatch.getDuration().toMillis());
    }

    private LocalDate parseOrDefaultCutoff(String prop) {
        if (prop == null || prop.isBlank()) {
            return LocalDate.now().minusYears(1);
        }
        return LocalDate.parse(prop);
    }
}
