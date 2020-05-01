package uk.gov.hmcts.reform.dm.exception;

public class DocumentUpdateException extends RuntimeException {
    public DocumentUpdateException(String message) {
        super(message);
    }
}
