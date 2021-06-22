package uk.gov.hmcts.dm.config.batch;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import lombok.AllArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import uk.gov.hmcts.dm.commandobject.DocumentUpdate;
import uk.gov.hmcts.dm.commandobject.UpdateDocumentsCommand;
import uk.gov.hmcts.dm.service.StoredDocumentService;

import java.io.*;
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
        BufferedReader bufferedReader = getCsvFile(client);
        List<DocumentUpdate> updates = bufferedReader
            .lines()
            .skip(1)
            .map(line -> createDocumentUpdate(line.split(",")))
            .collect(Collectors.toList());

        documentService.updateItems(new UpdateDocumentsCommand(null, updates));
        client.delete();
        IOUtils.closeQuietly(bufferedReader);
    }

    private DocumentUpdate createDocumentUpdate(String[] cells) {
        final UUID documentId = UUID.fromString(cells[4]);
        final HashMap<String, String> metadata = new HashMap<>();

        metadata.put("case_id", cells[1]);
        metadata.put("case_type_id", cells[2]);
        metadata.put("jurisdiction", cells[3]);

        return new DocumentUpdate(documentId, metadata);
    }
}
