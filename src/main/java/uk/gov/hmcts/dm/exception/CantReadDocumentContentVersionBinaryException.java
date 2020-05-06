package uk.gov.hmcts.dm.exception;

import lombok.Getter;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;

public class CantReadDocumentContentVersionBinaryException extends RuntimeException {

    @Getter
    private final transient DocumentContentVersion documentContentVersion;

    public CantReadDocumentContentVersionBinaryException() {
        super();
        this.documentContentVersion = null;
    }

    public CantReadDocumentContentVersionBinaryException(String message) {
        super(message);
        this.documentContentVersion = null;
    }

    public CantReadDocumentContentVersionBinaryException(String message, DocumentContentVersion documentContentVersion) {
        super(message);
        this.documentContentVersion = documentContentVersion;
    }

    public CantReadDocumentContentVersionBinaryException(Throwable cause, DocumentContentVersion documentContentVersion) {
        super(cause);
        this.documentContentVersion = documentContentVersion;
    }

    public CantReadDocumentContentVersionBinaryException(String message, Throwable cause) {
        super(message, cause);
        this.documentContentVersion = null;
    }

    public CantReadDocumentContentVersionBinaryException(Throwable cause) {
        super(cause);
        this.documentContentVersion = null;
    }

    protected CantReadDocumentContentVersionBinaryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.documentContentVersion = null;
    }
}
