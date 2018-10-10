package uk.gov.hmcts.dm.service;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.exception.FileStorageException;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;

@Slf4j
@Service
public class BlobStorageWriteService {

    private final CloudBlobContainer cloudBlobContainer;

    @Autowired
    public BlobStorageWriteService(CloudBlobContainer cloudBlobContainer) {
        this.cloudBlobContainer = cloudBlobContainer;
    }

    public String uploadDocumentContentVersion(@NotNull StoredDocument storedDocument,
                                             @NotNull DocumentContentVersion documentContentVersion,
                                             @NotNull MultipartFile multiPartFile) {
        try {
            CloudBlockBlob blob = getCloudFile(documentContentVersion.getId());
            blob.upload(multiPartFile.getInputStream(), documentContentVersion.getSize());
            log.debug("Uploaded content for document id: {} documentContentVersion id {}",
                storedDocument.getId(),
                documentContentVersion.getId());
            return blob.getUri().toString();
        } catch (URISyntaxException | StorageException | IOException e) {
            log.error("Exception caught", e);
            throw new FileStorageException(e, storedDocument.getId(), documentContentVersion.getId());
        }
    }

    private CloudBlockBlob getCloudFile(UUID uuid) throws StorageException, URISyntaxException {
        return cloudBlobContainer.getBlockBlobReference(uuid.toString());
    }
}
