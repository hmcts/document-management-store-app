package uk.gov.hmcts.dm.service;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.file.CloudFile;
import com.microsoft.azure.storage.file.CloudFileShare;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.URISyntaxException;
import java.util.UUID;

@Service
public class AzureFileStorageClient {

    @Autowired
    private CloudFileShare cloudFileShare;

    @PostConstruct
    void init() throws StorageException {
        cloudFileShare.createIfNotExists();
    }

    public CloudFile getCloudFile(@NonNull UUID uuid) throws StorageException, URISyntaxException {
        return cloudFileShare.getRootDirectoryReference().getFileReference(uuid.toString());
    }

}
