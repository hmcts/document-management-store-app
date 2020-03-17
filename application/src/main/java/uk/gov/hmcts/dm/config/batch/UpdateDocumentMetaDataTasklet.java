package uk.gov.hmcts.dm.config.batch;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.io.*;

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
            client.downloadToFile(csv.getAbsolutePath());
            final InputStream stream = new FileInputStream(csv);

            return new BufferedReader(new InputStreamReader(stream));
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

        client.delete();
    }
}
