package uk.gov.hmcts.dm.config.batch;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class UpdateDocumentMetaDataTasklet implements Tasklet {

    private final BlobContainerClient blobClient;

    public UpdateDocumentMetaDataTasklet(BlobContainerClient blobClient) {
        this.blobClient = blobClient;
    }

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

            return new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UpdateDocumentMetaDataException(e);
        }
    }

    private void processItem(BlobClient client) {
        final BufferedReader csv = getCsvFile(client);

        csv
            .lines()
            .skip(1)
            .map(i -> i.split(","))
            .forEach(row -> System.out.println(row));

//        client.delete();
    }
}
