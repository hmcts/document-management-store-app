package uk.gov.hmcts.dm.exception;

public class FileStorageException extends RuntimeException {
    public FileStorageException(Exception e) {
        super(e);
    }
}
