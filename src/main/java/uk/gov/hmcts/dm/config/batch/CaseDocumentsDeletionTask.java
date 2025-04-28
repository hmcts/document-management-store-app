package uk.gov.hmcts.dm.config.batch;

import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.dm.repository.StoredDocumentRepository;
import uk.gov.hmcts.dm.service.StoredDocumentService;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This task periodically checks for Case Documents marked for hard deletion.
 * If it finds one it will delete the document Binary from the blob storage and delete the related rows
 * from the database.
 */

@Service
public class CaseDocumentsDeletionTask implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(CaseDocumentsDeletionTask.class);

    private final StoredDocumentService storedDocumentService;

    private final StoredDocumentRepository storedDocumentRepository;

    @Value("${spring.batch.caseDocumentsDeletion.batchSize}")
    private int batchSize;

    @Value("${spring.batch.caseDocumentsDeletion.noOfIterations}")
    private int noOfIterations;

    @Value("${spring.batch.caseDocumentsDeletion.threadLimit}")
    private int threadLimit;

    public CaseDocumentsDeletionTask(StoredDocumentService storedDocumentService,
                                     StoredDocumentRepository storedDocumentRepository) {
        this.storedDocumentService = storedDocumentService;
        this.storedDocumentRepository = storedDocumentRepository;
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
                getAndDeleteCaseDocuments(i);
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

    private void getAndDeleteCaseDocuments(int i) {
        StopWatch iterationStopWatch = new StopWatch();
        iterationStopWatch.start();

        StopWatch dbGetQueryStopWatch = new StopWatch();
        dbGetQueryStopWatch.start();

        Pageable pageable = PageRequest.of(0, batchSize);

        List<UUID> storedDocuments = storedDocumentRepository.findCaseDocumentIdsForDeletion(pageable);

        dbGetQueryStopWatch.stop();
        log.info("Time taken to get {} rows from DB : {} ms", storedDocuments.size(),
                dbGetQueryStopWatch.getDuration().toMillis());

        if (CollectionUtils.isEmpty(storedDocuments)) {
            iterationStopWatch.stop();
            log.info("Time taken to complete empty iteration :  {} was : {} ms", i,
                    iterationStopWatch.getDuration().toMillis());
            return;
        }

        int batchCommitSize = 500; // Define the batch size for committing to the DB
        List<List<UUID>> batches = Lists.partition(storedDocuments, batchCommitSize);

        try (ExecutorService executorService = Executors.newFixedThreadPool(threadLimit)) {
            batches.forEach(
                    batch -> executorService.submit(() ->
                            storedDocumentService.deleteDocumentsDetails(batch))
            );
        }
        iterationStopWatch.stop();
        log.info("Time taken to complete iteration number :  {} was : {} ms", i,
                iterationStopWatch.getDuration().toMillis());
    }
}
