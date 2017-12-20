package uk.gov.hmcts.dm.exception;

public class DmRuntimeException extends RuntimeException {

    public DmRuntimeException(Exception e) {
        super(e);
    }

    public DmRuntimeException(String message) {
        super(message);
    }
}
