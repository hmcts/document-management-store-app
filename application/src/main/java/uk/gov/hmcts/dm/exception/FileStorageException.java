package uk.gov.hmcts.dm.exception;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.Getter;
import lombok.NonNull;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class FileStorageException extends RuntimeException {

    @Getter
    private final UUID documentId;

    @Getter
    private final UUID versionId;

    public FileStorageException(Exception e, @NonNull UUID documentId, @NonNull UUID versionId) {
        super(e);
        this.documentId = documentId;
        this.versionId = versionId;
    }

    public FileStorageException(@NonNull UUID documentId, @NonNull UUID versionId) {
        super();
        this.documentId = documentId;
        this.versionId = versionId;
    }

    @Override
    public String getMessage() {
        return String.format("Could not store the blob in Azure Blob Store for documentId=[%s] and versionId=[%s]",
            getDocumentId(), getVersionId());
    }

}
