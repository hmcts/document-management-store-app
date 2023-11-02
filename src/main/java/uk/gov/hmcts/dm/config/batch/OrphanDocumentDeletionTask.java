package uk.gov.hmcts.dm.config.batch;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.service.StoredDocumentService;
import uk.gov.hmcts.dm.service.batch.AuditedStoredDocumentBatchOperationsService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This task periodically checks for CSV files in the orphandocuments blob container.
 * If it finds one it will download
 * it and then delete all the documents found with the UUID provided in the file.
 * After the deletion has been completed the file
 * is removed from the blob container.
 */

@Service
@ConditionalOnProperty("toggle.orphandocumentdeletion")
@EnableScheduling
public class OrphanDocumentDeletionTask {
    private static final Logger log = LoggerFactory.getLogger(OrphanDocumentDeletionTask.class);
    private static final String TASK_NAME = "Orphan-Document-Deletion-Task";

    private final BlobContainerClient blobClient;
    private final StoredDocumentService documentService;
    private final AuditedStoredDocumentBatchOperationsService auditedStoredDocumentBatchOperationsService;

    public OrphanDocumentDeletionTask(
        @Qualifier("orphandocument-storage") BlobContainerClient blobClient,
        StoredDocumentService documentService,
        AuditedStoredDocumentBatchOperationsService auditedStoredDocumentBatchOperationsService
    ) {
        this.blobClient = blobClient;
        this.documentService = documentService;
        this.auditedStoredDocumentBatchOperationsService = auditedStoredDocumentBatchOperationsService;
    }

    @Scheduled(cron = "${spring.batch.orphanFileDeletionCronJobSchedule}")
    @SchedulerLock(name = TASK_NAME)
    public void execute() {

        log.info("==== Deletion of Orphan Documents started ====");
        Optional<BlobClient> blob = blobClient.listBlobs()
            .stream()
            .map(blobItem -> blobClient.getBlobClient(blobItem.getName()))
            .findAny();

        if (blob.isPresent()) {
            processItem(blob.get());
        }
        log.info("==== Deletion of Orphan Documents ended ====");

    }

    private Set<UUID> getCsvFileAndParse(BlobClient client) {
        File csv = null;
        try {
            csv = File.createTempFile("orphan-document", ".csv");
            final String fileName = csv.getAbsolutePath();

            client.downloadToFile(fileName);

            try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
                Set<UUID> set = stream
                    .flatMap(line -> Arrays.stream(line.split(",")))
                    .map(str -> {
                        try {
                            return UUID.fromString(str);
                        } catch (IllegalArgumentException e) {
                            log.info("Skipping {} as it is not UUID", str);
                            return null;
                        }
                    })
                    .filter(uuid -> uuid != null)
                    .collect(Collectors.toSet());

                return set;
            } catch (IOException e) {
                log.info("File Parsing failed", e);
            }

        } catch (Exception ex) {
            log.info("File processing failed", ex);

        } finally {
            try {
                if (csv != null && csv.exists()) {
                    csv.delete();
                }
            } catch (Exception ex) {
                log.info("Deleting temp file failed, {} ", csv.getPath());
            }
        }
        return null;
    }

    private void processItem(BlobClient client) {

        log.info("ProcessItem of Orphan Documents started for : {} ", client.getBlobName());
        try {
            Set<UUID> documentIds = getCsvFileAndParse(client);

            if (documentIds == null || documentIds.size() < 1) {
                log.info("No item found in the file to processed in {}", client.getBlobUrl());
                return;
            }
            log.info(" {} file processed ", client.getBlobName());

            for (UUID docId : documentIds) {
                Optional<StoredDocument> storedDocument = documentService.findOne(docId);
                if (storedDocument.isPresent()) {
                    auditedStoredDocumentBatchOperationsService.hardDeleteStoredDocument(storedDocument.get());
                } else {
                    log.error("Document with Id: {} not found.", docId);
                }
            }

            log.info("Orphan Documents {} from csv file with name {} ",
                documentIds.size(),
                client.getBlobName()
            );

            client.delete();
        } catch (Exception ex) {
            log.error("ProcessItem of Orphan Documents failed for : {} ", client.getBlobName(), ex);
        }

    }

}
