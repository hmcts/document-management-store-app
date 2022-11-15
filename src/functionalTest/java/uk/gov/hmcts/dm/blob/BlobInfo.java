package uk.gov.hmcts.dm.blob;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.specialized.BlobLeaseClient;

import java.util.Optional;

public class BlobInfo {
    private final BlobClient blobClient;
    private String leaseId;
    private BlobLeaseClient blobLeaseClient;

    public BlobInfo(BlobClient blobClient) {
        this.blobClient = blobClient;
    }

    public BlobClient getBlobClient() {
        return blobClient;
    }

    public boolean isLeased() {
        return Optional.ofNullable(leaseId).isPresent();
    }

    public void setLeaseId(String leaseId) {
        this.leaseId = leaseId;
    }

    public void setBlobLeaseClient(BlobLeaseClient blobLeaseClient) {
        this.blobLeaseClient = blobLeaseClient;
    }

    public void releaseLease() {
        blobLeaseClient.releaseLease();
    }
}
