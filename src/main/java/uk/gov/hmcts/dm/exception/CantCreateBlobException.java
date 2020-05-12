package uk.gov.hmcts.dm.exception;

public class CantCreateBlobException extends RuntimeException {
    public CantCreateBlobException(Exception e) {
        super(e);
    }
}
