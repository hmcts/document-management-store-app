package uk.gov.hmcts.reform.dm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class DocumentContentVersionNotFoundException extends RuntimeException {

    public DocumentContentVersionNotFoundException(String message) {
        super(message);
    }

}
