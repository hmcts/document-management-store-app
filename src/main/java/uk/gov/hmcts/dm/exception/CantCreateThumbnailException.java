package uk.gov.hmcts.dm.exception;

public class CantCreateThumbnailException extends RuntimeException {
    public CantCreateThumbnailException(Exception e) {
        super(e);
    }
}
