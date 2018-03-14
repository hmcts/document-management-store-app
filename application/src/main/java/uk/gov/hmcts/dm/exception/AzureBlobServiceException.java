package uk.gov.hmcts.dm.exception;

public class AzureBlobServiceException extends RuntimeException {
    public AzureBlobServiceException(Exception e) {
        super(e);
    }
}
