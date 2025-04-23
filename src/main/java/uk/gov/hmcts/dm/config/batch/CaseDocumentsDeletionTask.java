package uk.gov.hmcts.dm.config.batch;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.dm.service.StoredDocumentService;

/**
 * This task periodically checks for Case Documents marked for hard deletion.
 * If it finds one it will delete the document Binary from the blob storage and delete the related rows
 * from the database.
 */

@Component
@Transactional(propagation = Propagation.REQUIRED)
public class CaseDocumentsDeletionTask implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(CaseDocumentsDeletionTask.class);

    private final StoredDocumentService storedDocumentService;

    public CaseDocumentsDeletionTask(StoredDocumentService storedDocumentService) {
        this.storedDocumentService = storedDocumentService;
    }

    @Override
    public void run() {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        storedDocumentService.deleteCaseDocuments();
        stopWatch.stop();
        log.info("Deletion job for Case Docs took {} ms", stopWatch.getDuration().toMillis());
    }
}
