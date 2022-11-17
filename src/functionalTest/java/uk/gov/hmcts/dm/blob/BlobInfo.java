package uk.gov.hmcts.dm.blob;

import com.azure.storage.blob.BlobClient;

public class BlobInfo {
    private final BlobClient blobClient;

    public BlobInfo(BlobClient blobClient) {
        this.blobClient = blobClient;
    }

    public BlobClient getBlobClient() {
        return blobClient;
    }
}
