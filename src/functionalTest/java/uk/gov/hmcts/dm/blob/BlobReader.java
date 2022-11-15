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
    private final LeaseClientProvider leaseClientProvider;
    private final String containerName;
    private final int leaseTime;

    public BlobReader(
            BlobServiceClient blobServiceClient,
            LeaseClientProvider leaseClientProvider,
            @Value("${azure.storage.blob-container-reference}") String containerName,
            @Value("${azure.storage.leaseTime}") int leaseTime) {
        this.blobServiceClient =  blobServiceClient;
        this.leaseClientProvider = leaseClientProvider;
        this.containerName = containerName;
        this.leaseTime = leaseTime;
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
                .filter(blobInfo -> {
                    this.acquireLease(blobInfo);
                    return blobInfo.isLeased();
                })
                .findFirst();

    }

    private void acquireLease(BlobInfo blobInfo) {
        try {
            var blobLeaseClient = leaseClientProvider.get(blobInfo.getBlobClient());
            String leaseId = blobLeaseClient.acquireLease(leaseTime);
            blobInfo.setBlobLeaseClient(blobLeaseClient);
            blobInfo.setLeaseId(leaseId);
        } catch (Exception e) {
            LOG.error("Unable to acquire lease for blob {}",
                    blobInfo.getBlobClient().getBlobName());
        }
    }
}
