package uk.gov.hmcts.dm.exception;

import org.springframework.http.HttpStatus;

public class InvalidRequestException extends Exception {

    public static final long serialVersionUID = 432973322;

    private final HttpStatus errorCode;

    public InvalidRequestException(final String errorMessage, final HttpStatus errCode) {
        super(errorMessage);
        errorCode = errCode;
    }

    public HttpStatus getErrorCode() {
        return errorCode;
    }
}
