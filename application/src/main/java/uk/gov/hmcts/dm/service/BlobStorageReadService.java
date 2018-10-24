package uk.gov.hmcts.dm.service;

import com.microsoft.azure.storage.blob.CloudBlobContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.exception.CantReadDocumentContentVersionBinaryException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import static org.apache.commons.io.IOUtils.copy;


@Service
@Transactional
public class BlobStorageReadService {

    private final CloudBlobContainer cloudBlobContainer;

    @Autowired
    public BlobStorageReadService(CloudBlobContainer cloudBlobContainer) {
        this.cloudBlobContainer = cloudBlobContainer;
    }

    public void loadBlob(DocumentContentVersion documentContentVersion, OutputStream outputStream) {
        try {
            copy(new URL(documentContentVersion.getContentUri()).openStream(), outputStream);
        } catch (IOException e) {
            throw new CantReadDocumentContentVersionBinaryException(e, documentContentVersion);
        }
    }
}
