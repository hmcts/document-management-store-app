package uk.gov.hmcts.dm.blob;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.specialized.BlobLeaseClient;

public interface LeaseClientProvider {
    BlobLeaseClient get(BlobClient blobClient);
}
