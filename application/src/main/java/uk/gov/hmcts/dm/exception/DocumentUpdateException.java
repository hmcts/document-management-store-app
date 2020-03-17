package uk.gov.hmcts.dm.exception;

public class DocumentUpdateException extends RuntimeException {
    public DocumentUpdateException(String message) {
        super(message);
    }
}
