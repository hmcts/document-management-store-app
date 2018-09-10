package uk.gov.hmcts.dm.service;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.exception.FileStorageException;
import uk.gov.hmcts.dm.repository.DocumentContentVersionRepository;

import java.net.URISyntaxException;
import java.util.UUID;

@Service
public class BlobStorageDeleteService {

    private final CloudBlobContainer cloudBlobContainer;
    private final DocumentContentVersionRepository documentContentVersionRepository;

    @Autowired
    public BlobStorageDeleteService(final CloudBlobContainer cloudBlobContainer,
                                    final DocumentContentVersionRepository documentContentVersionRepository) {
        this.cloudBlobContainer = cloudBlobContainer;
        this.documentContentVersionRepository = documentContentVersionRepository;
    }

    public void delete(final UUID storedDocumentId, final DocumentContentVersion documentContentVersion) {
        try {
          cloudBlobContainer.getBlobReferenceFromServer(documentContentVersion.getId().toString()).deleteIfExists();
          documentContentVersion.setContentUri(null);
        } catch (URISyntaxException | StorageException e) {
            throw new FileStorageException( e, storedDocumentId, documentContentVersion.getId());
        }
    }
}
