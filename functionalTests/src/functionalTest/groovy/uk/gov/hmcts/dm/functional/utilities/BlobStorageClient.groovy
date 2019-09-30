package uk.gov.hmcts.dm.functional.utilities;

import com.microsoft.azure.storage.blob.CloudBlobContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class BlobStorageClient {

    @Autowired
    CloudBlobContainer cloudBlobContainer

    boolean doesDocumentExist(String id) {
        cloudBlobContainer.getBlockBlobReference(id).exists()
    }

}
