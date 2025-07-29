package uk.gov.hmcts.dm.service;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.options.BlockBlobOutputStreamOptions;
import com.azure.storage.blob.specialized.BlobOutputStream;
import com.azure.storage.blob.specialized.BlockBlobClient;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.exception.FileStorageException;
import uk.gov.hmcts.dm.repository.DocumentContentVersionRepository;

import java.io.IOException;
import java.util.UUID;

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

        try {
            BlockBlobClient blob = getCloudFile(documentContentVersion.getId());
            ParallelTransferOptions options = new ParallelTransferOptions()
                    .setBlockSizeLong(4L * 1024L * 1024L) // 4MB block size
                    .setMaxConcurrency(8); // 8 parallel threads

            BlockBlobOutputStreamOptions blockBlobOutputStreamOptions = new BlockBlobOutputStreamOptions();
            blockBlobOutputStreamOptions.setParallelTransferOptions(options);

            BlobOutputStream blobOutputStream = blob.getBlobOutputStream(blockBlobOutputStreamOptions);
            blobOutputStream.write(multiPartFile.getBytes());
            blobOutputStream.close();
            documentContentVersion.setContentUri(blob.getBlobUrl());
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
