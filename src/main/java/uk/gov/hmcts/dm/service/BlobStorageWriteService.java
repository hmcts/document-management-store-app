package uk.gov.hmcts.dm.service;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.specialized.BlockBlobClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.exception.FileStorageException;
import uk.gov.hmcts.dm.repository.DocumentContentVersionRepository;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import javax.validation.constraints.NotNull;

@Slf4j
@Service
public class BlobStorageWriteService {

    private BlobContainerClient cloudBlobContainer;
    private DocumentContentVersionRepository documentContentVersionRepository;

    @Autowired
    public BlobStorageWriteService(BlobContainerClient cloudBlobContainer,
                                   DocumentContentVersionRepository documentContentVersionRepository) {
        this.cloudBlobContainer = cloudBlobContainer;
        this.documentContentVersionRepository = documentContentVersionRepository;
    }

    public void uploadDocumentContentVersion(@NotNull StoredDocument storedDocument,
                                             @NotNull DocumentContentVersion documentContentVersion,
                                             @NotNull MultipartFile multiPartFile) {
        writeBinaryStream(storedDocument.getId(), documentContentVersion, multiPartFile);
        documentContentVersionRepository.updateContentUriAndContentCheckSum(
            documentContentVersion.getId(),
            documentContentVersion.getContentUri(),
            documentContentVersion.getContentChecksum());
    }

    private void writeBinaryStream(UUID documentId,
                                   DocumentContentVersion documentContentVersion,
                                   MultipartFile multiPartFile) {
        log.debug("Uploading document {} / version {} to Azure Blob Storage...",
                  documentId,
                  documentContentVersion.getId());

        try (
            final InputStream inputStream = new BufferedInputStream(multiPartFile.getInputStream())
        ) {
            BlockBlobClient blob = getCloudFile(documentContentVersion.getId());
            blob.upload(inputStream, documentContentVersion.getSize());

            documentContentVersion.setContentUri(blob.getBlobUrl());
            log.info("Uploading document {} / version {} to Azure Blob Storage: OK: uri {}, size = {}",
                      documentId,
                      documentContentVersion.getId(),
                      blob.getBlobUrl(),
                      documentContentVersion.getSize());
        } catch (IOException e) {
            log.warn("Uploading document {} / version {} to Azure Blob Storage: FAILED",
                     documentId,
                     documentContentVersion.getId());
            throw new FileStorageException(e, documentId, documentContentVersion.getId());
        }
    }

    private BlockBlobClient getCloudFile(UUID uuid) {
        return cloudBlobContainer.getBlobClient(uuid.toString()).getBlockBlobClient();
    }

}
