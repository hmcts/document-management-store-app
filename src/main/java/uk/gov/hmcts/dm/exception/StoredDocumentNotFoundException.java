package uk.gov.hmcts.dm.exception;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

/**
 * Created by pawel on 13/10/2017.
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class StoredDocumentNotFoundException extends RuntimeException {

    @Getter
    @Setter
    private UUID uuid;

    public StoredDocumentNotFoundException(@NonNull UUID uuid) {
        this.setUuid(uuid);
    }

    @Override
    public String getMessage() {
        return String.format("Document with ID: %s could not be found", uuid.toString());
    }

}
