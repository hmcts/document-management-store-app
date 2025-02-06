package uk.gov.hmcts.dm.config.batch;

import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.dm.service.StoredDocumentService;

/**
 * This task periodically checks for Case Documents marked for hard deletion.
 * If it finds one it will delete the document Binary from the blob storage and delete the related rows
 * from the database.
 */

@Service
@ConditionalOnProperty("toggle.casedocumentsdeletion")
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT5M")
@Transactional(propagation = Propagation.REQUIRED)
public class CaseDocumentsDeletionTask {

    private static final Logger log = LoggerFactory.getLogger(CaseDocumentsDeletionTask.class);

    private final StoredDocumentService storedDocumentService;

    public CaseDocumentsDeletionTask(StoredDocumentService storedDocumentService) {
        this.storedDocumentService = storedDocumentService;
    }

    @Scheduled(cron = "${spring.batch.caseDocumentsDeletionJobSchedule}")
    @SchedulerLock(name = "${task.env}-Case-Documents-Deletion-Task")
    public void execute() {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        storedDocumentService.deleteCaseDocuments();
        stopWatch.stop();
        log.info("Deletion job for Case Docs took {} ms", stopWatch.getDuration().toMillis());

    }

}
