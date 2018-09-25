package uk.gov.hmcts.dm.service;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.exception.FileStorageException;

import java.net.URISyntaxException;
import java.util.UUID;

@Service
public class BlobStorageDeleteService {

    private final CloudBlobContainer cloudBlobContainer;

    @Autowired
    public BlobStorageDeleteService(final CloudBlobContainer cloudBlobContainer) {
        this.cloudBlobContainer = cloudBlobContainer;
    }

    public void deleteIfExists(final UUID storedDocumentId, final DocumentContentVersion documentContentVersion) {
        try {
            cloudBlobContainer.getBlockBlobReference(documentContentVersion.getId().toString());
            // Todo RDM-2913 .deleteIfExists();
            documentContentVersion.setContentUri(null);
        } catch (URISyntaxException | StorageException e) {
            throw new FileStorageException(e, storedDocumentId, documentContentVersion.getId());
        }
    }
}
