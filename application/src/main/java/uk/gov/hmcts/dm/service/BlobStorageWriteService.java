package uk.gov.hmcts.dm.service;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.exception.FileStorageException;
import uk.gov.hmcts.dm.repository.DocumentContentVersionRepository;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;

@Service
public class BlobStorageWriteService {

    private static final Logger LOG = LoggerFactory.getLogger(BlobStorageWriteService.class);

    private final CloudBlobContainer cloudBlobContainer;
    private final DocumentContentVersionRepository documentContentVersionRepository;

    @Autowired
    public BlobStorageWriteService(CloudBlobContainer cloudBlobContainer,
                                   DocumentContentVersionRepository documentContentVersionRepository) {
        this.cloudBlobContainer = cloudBlobContainer;
        this.documentContentVersionRepository = documentContentVersionRepository;
    }

    public void uploadDocumentContentVersion(@NotNull StoredDocument storedDocument,
                                             @NotNull DocumentContentVersion documentContentVersion,
                                             @NotNull MultipartFile multiPartFile) {
        writeBinaryStream(storedDocument.getId(), documentContentVersion, multiPartFile);
        documentContentVersionRepository.update(documentContentVersion.getId(), documentContentVersion.getContentUri());
    }

    private void writeBinaryStream(UUID documentId,
                                   DocumentContentVersion documentContentVersion,
                                   MultipartFile multiPartFile) {
        try {
            CloudBlockBlob blob = getCloudFile(documentContentVersion.getId());
            blob.upload(multiPartFile.getInputStream(), documentContentVersion.getSize());
            documentContentVersion.setContentUri(blob.getUri().toString());
            LOG.debug("Uploaded content for document id: {} documentContentVersion id {}",
                      documentId,
                      documentContentVersion.getId());
        } catch (URISyntaxException | StorageException | IOException e) {
            throw new FileStorageException(e, documentId, documentContentVersion.getId());
        }
    }

    private CloudBlockBlob getCloudFile(UUID uuid) throws StorageException, URISyntaxException {
        return cloudBlobContainer.getBlockBlobReference(uuid.toString());
    }
}
