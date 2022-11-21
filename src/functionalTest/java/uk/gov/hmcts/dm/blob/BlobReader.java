package uk.gov.hmcts.dm.blob;

import com.azure.storage.blob.BlobServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@ConditionalOnExpression("!T(org.springframework.util.StringUtils).isEmpty('${azure.storage.connection-string:}')")
public class BlobReader {

    private static final Logger LOG = LoggerFactory.getLogger(BlobReader.class);

    private final BlobServiceClient blobServiceClient;
    private final String containerName;

    public BlobReader(
            BlobServiceClient blobServiceClient,
            @Value("${azure.storage.blob-container-reference}") String containerName) {
        this.blobServiceClient =  blobServiceClient;
        this.containerName = containerName;
    }

    public Optional<BlobInfo> retrieveBlobToProcess(String blobName) {
        LOG.info("About to read blob from container {}", containerName);
        var containerClient = blobServiceClient.getBlobContainerClient(containerName);

        return containerClient.listBlobs().stream()
                .filter(blobItem -> blobItem.getName().equals(blobName))
                .map(blobItem ->
                    new BlobInfo(
                        containerClient.getBlobClient(blobItem.getName())
                    )
                )
                .findFirst();

    }
}
