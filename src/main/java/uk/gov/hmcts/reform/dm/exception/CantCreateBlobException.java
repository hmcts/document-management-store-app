package uk.gov.hmcts.reform.dm.exception;

public class CantCreateBlobException extends RuntimeException {
    public CantCreateBlobException(Exception e) {
        super(e);
    }
}
