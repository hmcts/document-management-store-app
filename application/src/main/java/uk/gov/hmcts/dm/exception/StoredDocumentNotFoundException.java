package uk.gov.hmcts.dm.exception;

import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class StoredDocumentNotFoundException extends ResourceNotFoundException {

    public StoredDocumentNotFoundException(@NonNull UUID uuid) {
        super(uuid);
    }

    @Override
    public String getMessage() {
        return String.format("Document with ID: %s could not be found", getUuid().toString());
    }

}
