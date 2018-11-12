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
import uk.gov.hmcts.dm.repository.DocumentContentVersionRepository;

import javax.validation.constraints.NotNull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.UUID;

import static org.apache.commons.io.IOUtils.toByteArray;
import static org.springframework.security.core.token.Sha512DigestUtils.shaHex;

@Slf4j
@Service
public class BlobStorageWriteService {

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
        documentContentVersionRepository.updateContentUriAndContentCheckSum(documentContentVersion.getId(),
                                                                            documentContentVersion.getContentUri(),
                                                                            documentContentVersion.getContentChecksum());
    }

    private void writeBinaryStream(UUID documentId,
                                   DocumentContentVersion documentContentVersion,
                                   MultipartFile multiPartFile) {
        try (final InputStream inputStream = multiPartFile.getInputStream()) {
            CloudBlockBlob blob = getCloudFile(documentContentVersion.getId());
            blob.upload(inputStream, documentContentVersion.getSize());
            final byte[] bytes = toByteArray(inputStream);
            documentContentVersion.setContentUri(blob.getUri().toString());
            final String checksum = shaHex(bytes);
            documentContentVersion.setContentChecksum(checksum);
            log.debug("Uploaded content for document id: {} documentContentVersion id {} to {}; Size = {} checksum = {}",
                      documentId,
                      documentContentVersion.getId(),
                      blob.getUri(),
                      documentContentVersion.getSize(),
                      checksum);

            // checks that we uploaded correctly
            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            blob.download(byteArrayOutputStream);
            if (! checksum.equals(shaHex(byteArrayOutputStream.toByteArray()))) {
                throw new FileStorageException(documentId, documentContentVersion.getId());
            }
        } catch (URISyntaxException | StorageException | IOException e) {
            log.error("Exception caught with documentContentVersion, id = {}", documentContentVersion.getId(), e);
            throw new FileStorageException(e, documentId, documentContentVersion.getId());
        }
    }

    private CloudBlockBlob getCloudFile(UUID uuid) throws StorageException, URISyntaxException {
        return cloudBlobContainer.getBlockBlobReference(uuid.toString());
    }
}
