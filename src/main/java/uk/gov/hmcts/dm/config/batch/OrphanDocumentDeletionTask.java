package uk.gov.hmcts.dm.config.batch;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.service.StoredDocumentService;
import uk.gov.hmcts.dm.service.batch.AuditedStoredDocumentBatchOperationsService;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
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
@EnableSchedulerLock(defaultLockAtMostFor = "PT5M")
@Transactional(propagation = Propagation.REQUIRED)
public class OrphanDocumentDeletionTask {
    private static final Logger log = LoggerFactory.getLogger(OrphanDocumentDeletionTask.class);
    private final BlobContainerClient blobClient;
    private final StoredDocumentService documentService;
    private final AuditedStoredDocumentBatchOperationsService auditedStoredDocumentBatchOperationsService;
    private static String TMP_DIR = System.getProperty("java.io.tmpdir");
    private static String FILE_NAME_REGEX =  "^(EM-\\d+|CHG.*|INC.*)\\.csv$";
    private static String SERVICE_NAME = "orphan-document-deletion";


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
    @SchedulerLock(name = "${task.env}-Orphan-Document-Deletion-Task")
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
        String csvPath = null;
        try {
            csvPath = TMP_DIR + File.separatorChar + "orphan-document.csv";
            client.downloadToFile(csvPath);

            try (Stream<String> stream = Files.lines(Paths.get(csvPath), StandardCharsets.UTF_8)) {
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
                if (csvPath != null) {
                    Files.delete(Paths.get(csvPath));
                }
            } catch (Exception ex) {
                log.info("Deleting temp file failed, {} ", csvPath);
            }
        }
        return null;
    }

    private String extractTicketNumFromFileName(String blobName) {
        if (!Pattern.matches(FILE_NAME_REGEX, blobName)) {
            throw new IllegalArgumentException("File should be ticket Number and extension should be '.csv',"
                + " Invalid file name:" + blobName);
        }
        return blobName.replace(".csv","");
    }

    private void processItem(BlobClient client) {

        var blobName = client.getBlobName();
        log.info("ProcessItem of Orphan Documents started for : {} ", blobName);
        try {
            String ticketNumber = extractTicketNumFromFileName(blobName);
            Set<UUID> documentIds = getCsvFileAndParse(client);

            if (documentIds == null || documentIds.isEmpty()) {
                log.info("No orphan item found in the file to processed in {}", client.getBlobUrl());
                return;
            }

            for (UUID docId : documentIds) {
                Optional<StoredDocument> storedDocument = documentService.findOne(docId);
                if (storedDocument.isPresent()) {
                    auditedStoredDocumentBatchOperationsService.hardDeleteStoredDocument(
                        storedDocument.get(),
                        ticketNumber,
                        SERVICE_NAME
                    );
                } else {
                    log.error("Document with Id: {} not found.", docId);
                }
            }

            log.info("Num of Orphan Documents: {} csv file name {} ",
                documentIds.size(),
                client.getBlobName()
            );


        } catch (Exception ex) {
            log.error("ProcessItem of Orphan Documents failed for : {} ", client.getBlobName(), ex);
        } finally {
            client.delete();
        }

    }

}
