package uk.gov.hmcts.dm.config.batch;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.dm.repository.DocumentContentVersionRepository;
import uk.gov.hmcts.dm.service.DocumentContentVersionService;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * This task periodically checks for Document Content Versions where the mimeTypeUpdated flag is false.
 * It will then read the blob from storage, detect the correct MIME type, and update the database record.
 */
@Service
public class MimeTypeUpdateTask implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(MimeTypeUpdateTask.class);

    private final DocumentContentVersionService documentContentVersionService;
    private final DocumentContentVersionRepository documentContentVersionRepository;

    @Value("${spring.batch.mimeTypeUpdate.batchSize}")
    private int batchSize;

    @Value("${spring.batch.mimeTypeUpdate.noOfIterations}")
    private int noOfIterations;

    @Value("${spring.batch.mimeTypeUpdate.threadLimit}")
    private int threadLimit;

    public MimeTypeUpdateTask(DocumentContentVersionService documentContentVersionService,
                              DocumentContentVersionRepository documentContentVersionRepository) {
        this.documentContentVersionService = documentContentVersionService;
        this.documentContentVersionRepository = documentContentVersionRepository;
    }

    @Override
    @Transactional
    public void run() {
        log.info("Started MIME Type Update job.");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            log.info("threadLimit: {}, noOfIterations: {}, batchSize: {}", threadLimit, noOfIterations, batchSize);

            for (int i = 0; i < noOfIterations; i++) {
                if (!getAndUpdateMimeTypes(i)) {
                    // Stop iterating if a run finds no records to process
                    log.info("No records found in iteration {}. Stopping job.", i);
                    break;
                }
            }

        } catch (Exception e) {
            log.error("MIME Type Update job failed with Error message: {}", e.getMessage(), e);
        } finally {
            stopWatch.stop();
            log.info("MIME Type Update job finished and took {} ms", stopWatch.getDuration().toMillis());
        }
    }

    private boolean getAndUpdateMimeTypes(int iteration) {
        StopWatch iterationStopWatch = new StopWatch();
        iterationStopWatch.start();

        Pageable pageable = PageRequest.of(0, batchSize);

        List<UUID> documentIds = documentContentVersionRepository
            .findDocumentContentVersionIdsForMimeTypeUpdate(pageable);

        if (CollectionUtils.isEmpty(documentIds)) {
            iterationStopWatch.stop();
            log.info("Iteration {}: No records found for MIME type update. Total time: {} ms",
                iteration, iterationStopWatch.getDuration().toMillis());
            return false; // Indicates no records were found
        }

        log.info("Iteration {}: Found {} records to process for MIME type update.", iteration, documentIds.size());

        ExecutorService executorService = Executors.newFixedThreadPool(threadLimit);
        try {
            documentIds.forEach(
                id -> executorService.submit(() -> documentContentVersionService.updateMimeType(id))
            );
        } finally {
            executorService.shutdown();
        }

        try {
            // Wait for all tasks to complete
            executorService.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("MIME type update job was interrupted while waiting for tasks to complete.", e);
        }

        iterationStopWatch.stop();
        log.info("Time taken to complete iteration number: {} was : {} ms", iteration,
            iterationStopWatch.getDuration().toMillis());
        return true; // Indicates records were processed
    }
}
