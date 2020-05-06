package uk.gov.hmcts.dm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
public class ValidationErrorException extends RuntimeException {

    public ValidationErrorException(String message) {
        super(message);
    }

    public ValidationErrorException(Throwable t) {
        super(t);
    }
}
