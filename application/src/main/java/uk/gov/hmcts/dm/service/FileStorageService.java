package uk.gov.hmcts.dm.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;

import java.io.OutputStream;
import java.util.UUID;

@Service
public class FileStorageService {

    //@Value("#{ '${file-storage-client}' == 'azureFileStorageClient' ? azureFileStorageClient : azureBlobStorageClient}")
    @Autowired
    private AzureBlobStorageClient fileStorageClient;

    public DocumentContentVersion uploadFile(StoredDocument storedDocument, MultipartFile file, String creatorId) {
        UUID uuid = UUID.randomUUID();
        fileStorageClient.uploadFile(uuid, file);

        return new DocumentContentVersion(
            uuid,
            storedDocument,
            file,
            creatorId);
    }

    public void delete(DocumentContentVersion documentContentVersion) {
        fileStorageClient.deleteFile(documentContentVersion.getId());
    }

    public void streamBinary(DocumentContentVersion documentContentVersion, OutputStream outputStream) {
        fileStorageClient.streamFileContent(documentContentVersion.getId(), outputStream);
    }

}
