package uk.gov.hmcts.reform.dm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by pawel on 13/10/2017.
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class StoredDocumentNotFoundException extends RuntimeException {

    public StoredDocumentNotFoundException(String message) {
        super(message);
    }

}
