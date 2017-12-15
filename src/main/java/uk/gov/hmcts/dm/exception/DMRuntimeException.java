package uk.gov.hmcts.dm.exception;

public class DMRuntimeException extends RuntimeException {

    public DMRuntimeException(Exception e) {
        super(e);
    }

    public DMRuntimeException(String message) {super(message);}
}
