package uk.gov.hmcts.dm.config.batch;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import lombok.AllArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.service.StoredDocumentService;
import uk.gov.hmcts.dm.service.batch.AuditedStoredDocumentBatchOperationsService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * This tasklet periodically checks for CSV files in the orphandocuments blob container. If it finds one it will download
 * it and then delete all the documents found with the UUID provided in the file. After the deletion has been completed the file
 * is removed from the blob container.
 */
@AllArgsConstructor
public class OrphanDocumentDeletionTasklet implements Tasklet {

    private static final UUID DUMMY_UUID = UUID.randomUUID();

    private static final Logger log = LoggerFactory.getLogger(OrphanDocumentDeletionTasklet.class);

    private final BlobContainerClient blobClient;
    private final StoredDocumentService documentService;
    private final AuditedStoredDocumentBatchOperationsService auditedStoredDocumentBatchOperationsService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {

        log.info("==== Deletion of Orphan Documents started ====");
        Optional<BlobClient>  blob = blobClient.listBlobs()
            .stream()
            .map(blobItem -> blobClient.getBlobClient(blobItem.getName()))
            .findAny();

        if (blob.isPresent()) {
            processItem(blob.get());
        }
        log.info("==== Deletion of Orphan Documents ended ====");
        return RepeatStatus.FINISHED;
    }

    private BufferedReader getCsvFile(BlobClient client) {
        try {
            final File csv = File.createTempFile("orphandocument", ".csv");
            final String filename = csv.getAbsolutePath();

            csv.delete();
            client.downloadToFile(filename);

            final InputStream stream = new FileInputStream(filename);

            return new BufferedReader(new InputStreamReader(stream));
        } catch (IOException e) {
            throw new UpdateDocumentMetaDataException(e);
        }
    }

    private void processItem(BlobClient client) {

        log.info("processItem of Orphan Documents started for : {} ", client.getBlobName());
        StopWatch stopwatch = new StopWatch();
        stopwatch.start();

        BufferedReader bufferedReader = null;
        try {
            bufferedReader = getCsvFile(client);
            List<UUID> documentIds = bufferedReader
                .lines()
                .skip(1)
                .map(line -> getDocumentId(line.split(",")))
                .collect(Collectors.toList());
            log.info(" {} file processed ", client.getBlobName());


            List<UUID> missingDocIds = new ArrayList<>();

            for (UUID docId : documentIds) {
                Optional<StoredDocument> storedDocument = documentService.findOne(docId);
                if (storedDocument.isPresent()) {
                    auditedStoredDocumentBatchOperationsService.hardDeleteStoredDocument(storedDocument.get());
                } else {
                    log.error("Document with Id: {} not found.",docId);
                }
            }

            log.info("DB updated for Orphan Documents: {}",client.getBlobName());

            stopwatch.stop();
            long timeElapsed = stopwatch.getTime();

            log.info("Time taken to delete {} Orphan Documents is  : {} milliseconds from csv file with name {} ", documentIds.size(),
                timeElapsed, client.getBlobName());

            client.delete();
        } finally {
            IOUtils.closeQuietly(bufferedReader);
        }
    }

    private UUID getDocumentId(String[] cells) {
        try {
            return UUID.fromString(cells[0]);
        } catch (IllegalArgumentException e) {
            log.error("Can not read Document UUID : {} ", cells[0]);
            return DUMMY_UUID;
        }
    }
}
