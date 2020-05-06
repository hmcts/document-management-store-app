package uk.gov.hmcts.dm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class CantCreateThumbnailException extends RuntimeException {
    public CantCreateThumbnailException(Exception e) {
        super(e);
    }

    public CantCreateThumbnailException(String message) {
        super(message);
    }
}
