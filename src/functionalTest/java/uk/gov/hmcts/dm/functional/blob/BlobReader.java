package uk.gov.hmcts.dm.functional.blob;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.specialized.BlockBlobClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BlobReader {

    private final BlobServiceClient blobServiceClient;
    private final String containerName;

    public BlobReader(
            BlobServiceClient blobServiceClient,
            @Value("${azure.storage.blob-container-reference}") String containerName) {
        this.blobServiceClient =  blobServiceClient;
        this.containerName = containerName;
    }

    public BlockBlobClient retrieveBlobToProcess(String blobName) {
        var containerClient = blobServiceClient.getBlobContainerClient(containerName);
        return containerClient.getBlobClient(blobName).getBlockBlobClient();
    }
}
