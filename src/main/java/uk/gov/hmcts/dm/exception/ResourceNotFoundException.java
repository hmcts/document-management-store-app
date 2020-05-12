package uk.gov.hmcts.dm.exception;

import lombok.Getter;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    @Getter
    private final UUID uuid;

    public ResourceNotFoundException(@NonNull UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public String getMessage() {
        return String.format("Document with ID: %s could not be found", uuid.toString());
    }

}
