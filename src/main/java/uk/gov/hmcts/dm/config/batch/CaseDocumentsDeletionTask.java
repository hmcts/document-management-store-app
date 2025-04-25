package uk.gov.hmcts.dm.config.batch;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.dm.service.StoredDocumentService;

/**
 * This task periodically checks for Case Documents marked for hard deletion.
 * If it finds one it will delete the document Binary from the blob storage and delete the related rows
 * from the database.
 */

@Service
public class CaseDocumentsDeletionTask implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(CaseDocumentsDeletionTask.class);

    private final StoredDocumentService storedDocumentService;


    @Value("${spring.batch.caseDocumentsDeletion.batchSize}")
    private int batchSize;

    @Value("${spring.batch.caseDocumentsDeletion.noOfIterations}")
    private int noOfIterations;

    @Value("${spring.batch.caseDocumentsDeletion.threadLimit}")
    private int threadLimit;

    public CaseDocumentsDeletionTask(StoredDocumentService storedDocumentService) {
        this.storedDocumentService = storedDocumentService;
    }

    @Override
    public void run() {
        log.info("Started Deletion job for Case Docs");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            log.info("threadLimit is : {}  and noOfIterations is {} and batchSize is : {}", threadLimit, noOfIterations,
                    batchSize);

            for (int i = 0; i < noOfIterations; i++) {
                storedDocumentService.getAndDeleteCaseDocuments(i,
                        batchSize, threadLimit);

            }
        } catch (Exception e) {
            stopWatch.stop();
            log.error("Deletion job for Case Docs failed with Error message : {} in {} ms",
                    e.getMessage(), stopWatch.getDuration().toMillis());
            return;
        }
        stopWatch.stop();
        log.info("Deletion job for Case Docs took {} ms", stopWatch.getDuration().toMillis());
    }

}
