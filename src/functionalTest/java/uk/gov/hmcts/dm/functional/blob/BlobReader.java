package uk.gov.hmcts.dm.functional.blob;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.specialized.BlockBlobClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

@Component
//@ConditionalOnBean(BlobServiceClient.class)
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

    public BlockBlobClient retrieveBlobToProcess(String blobName) {
        LOG.info("About to read blob from container {}", containerName);
        var containerClient = blobServiceClient.getBlobContainerClient(containerName);
        return containerClient.getBlobClient(blobName).getBlockBlobClient();
    }
}
