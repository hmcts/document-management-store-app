package uk.gov.hmcts.dm.exception;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.NonNull;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class DocumentContentVersionNotFoundException extends ResourceNotFoundException {

    public DocumentContentVersionNotFoundException(@NonNull UUID uuid) {
        super(uuid);
    }

    @Override
    public String getMessage() {
        return String.format("DocumentContentVersion with ID: %s could not be found", getUuid().toString());
    }

}
