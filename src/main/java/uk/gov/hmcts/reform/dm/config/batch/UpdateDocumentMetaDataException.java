package uk.gov.hmcts.reform.dm.config.batch;

public class UpdateDocumentMetaDataException extends RuntimeException {
    public UpdateDocumentMetaDataException(Exception e) {
        super(e);
    }
}
