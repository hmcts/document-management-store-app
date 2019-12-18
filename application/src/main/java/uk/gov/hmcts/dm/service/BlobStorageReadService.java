package uk.gov.hmcts.dm.service;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.exception.CantReadDocumentContentVersionBinaryException;

import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class BlobStorageReadService {

    private final CloudBlobContainer cloudBlobContainer;

    @Autowired
    public BlobStorageReadService(CloudBlobContainer cloudBlobContainer) {
        this.cloudBlobContainer = cloudBlobContainer;
    }

    public void loadBlob(DocumentContentVersion documentContentVersion, OutputStream outputStream) {
        log.debug("Reading document version {} from Azure Blob Storage...", documentContentVersion.getId());
        try {
            CloudBlockBlob blob = loadBlob(documentContentVersion.getId().toString());
            blob.download(outputStream);
            log.debug("Reading document version {} from Azure Blob Storage: OK", documentContentVersion.getId());
        } catch (URISyntaxException | StorageException e) {
            log.warn("Reading document version {} from Azure Blob Storage: FAILED", documentContentVersion.getId());
            throw new CantReadDocumentContentVersionBinaryException(e, documentContentVersion);
        }
    }

    private CloudBlockBlob loadBlob(String id) throws URISyntaxException, StorageException {
        return cloudBlobContainer.getBlockBlobReference(id);
    }

    public boolean doesBinaryExist(UUID uuid) throws URISyntaxException, StorageException {
        return loadBlob(uuid.toString()).exists();
    }

}
