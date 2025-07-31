package uk.gov.hmcts.dm.service;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.options.BlockBlobOutputStreamOptions;
import com.azure.storage.blob.specialized.BlobOutputStream;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.common.implementation.Constants;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${azure.upload.blockSize}")
    private int blockSize;

    @Value("${azure.upload.maxSingleUploadSize}")
    private int maxSingleUploadSize;

    @Value("${azure.upload.maxConcurrency}")
    private int maxConcurrency;

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

            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            BlockBlobClient blob = getCloudFile(documentContentVersion.getId());
            // The default values in BlockBlobClient (Azure Storage Blob SDK v12+) for
            // uploading with ParallelTransferOptions are:
            // Block size: 4 MB (4 * 1024 * 1024 bytes)
            // Max concurrency: 5
            // These defaults apply when you do not explicitly set ParallelTransferOptions during upload
            ParallelTransferOptions options = new ParallelTransferOptions()
                    .setBlockSizeLong(blockSize * Long.valueOf(Constants.MB)) // 8MB block size
                    .setMaxConcurrency(maxConcurrency)  // 10 parallel threads
                    .setMaxSingleUploadSizeLong(maxSingleUploadSize * Long.valueOf(Constants.MB));

            BlockBlobOutputStreamOptions blockBlobOutputStreamOptions = new BlockBlobOutputStreamOptions();
            blockBlobOutputStreamOptions.setParallelTransferOptions(options);

            BlobOutputStream blobOutputStream = blob.getBlobOutputStream(blockBlobOutputStreamOptions);
            blobOutputStream.write(multiPartFile.getBytes());
            blobOutputStream.close();
            documentContentVersion.setContentUri(blob.getBlobUrl());

            stopWatch.stop();
            log.info("Doc Id {} with size {}  took {} ms", documentId, documentContentVersion.getSize(),
                    stopWatch.getDuration().toMillis());

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
