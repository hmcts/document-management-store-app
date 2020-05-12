package uk.gov.hmcts.dm.service;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.specialized.BlockBlobClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;

import java.io.OutputStream;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class BlobStorageReadService {

    private final BlobContainerClient cloudBlobContainer;

    @Autowired
    public BlobStorageReadService(BlobContainerClient cloudBlobContainer) {
        this.cloudBlobContainer = cloudBlobContainer;
    }

    public void loadBlob(DocumentContentVersion documentContentVersion, OutputStream outputStream) {
        log.debug("Reading document version {} from Azure Blob Storage...", documentContentVersion.getId());
        BlockBlobClient blob = loadBlob(documentContentVersion.getId().toString());
        blob.download(outputStream);
        log.debug("Reading document version {} from Azure Blob Storage: OK", documentContentVersion.getId());
    }

    private BlockBlobClient loadBlob(String id) {
        return cloudBlobContainer.getBlobClient(id).getBlockBlobClient();
    }

    public boolean doesBinaryExist(UUID uuid) {
        return loadBlob(uuid.toString()).exists();
    }

}
