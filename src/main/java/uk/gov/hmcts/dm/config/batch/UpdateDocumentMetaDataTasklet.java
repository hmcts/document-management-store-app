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
import uk.gov.hmcts.dm.commandobject.DocumentUpdate;
import uk.gov.hmcts.dm.commandobject.UpdateDocumentsCommand;
import uk.gov.hmcts.dm.service.StoredDocumentService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * This tasklet periodically checks for CSV files in the hmctsmetadata blob container. If it finds one it will download
 * it and then update all the documents with the metadata in the file. After the update has been completed the file
 * is removed from the blob container.
 */
@AllArgsConstructor
public class UpdateDocumentMetaDataTasklet implements Tasklet {

    private static final Logger log = LoggerFactory.getLogger(UpdateDocumentMetaDataTasklet.class);

    private final BlobContainerClient blobClient;
    private final StoredDocumentService documentService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        blobClient.listBlobs()
            .stream()
            .parallel()
            .map(blobItem -> blobClient.getBlobClient(blobItem.getName()))
            .forEach(this::processItem);

        return RepeatStatus.FINISHED;
    }

    private BufferedReader getCsvFile(BlobClient client) {
        try {
            final File csv = File.createTempFile("metadata", ".csv");
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

        StopWatch stopwatch = new StopWatch();
        stopwatch.start();

        BufferedReader bufferedReader = getCsvFile(client);
        List<DocumentUpdate> updates = bufferedReader
            .lines()
            .skip(1)
            .map(line -> createDocumentUpdate(line.split(",")))
            .collect(Collectors.toList());

        documentService.updateItems(new UpdateDocumentsCommand(null, updates));

        stopwatch.stop();
        long timeElapsed = stopwatch.getTime();

        log.info("Time taken to update {} documents is  : {} milliseconds from csv file with name {} ", updates.size(),
            timeElapsed, client.getBlobName());

        client.delete();
        IOUtils.closeQuietly(bufferedReader);
    }

    private DocumentUpdate createDocumentUpdate(String[] cells) {
        final UUID documentId = UUID.fromString(cells[3]);
        final HashMap<String, String> metadata = new HashMap<>();

        metadata.put("case_id", cells[0]);
        metadata.put("case_type_id", cells[1]);
        metadata.put("jurisdiction", cells[2]);

        return new DocumentUpdate(documentId, metadata);
    }
}
