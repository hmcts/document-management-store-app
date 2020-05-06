package uk.gov.hmcts.dm.exception;

public class AppConfigurationException extends RuntimeException {

    public AppConfigurationException(String message) {
        super(message);
    }

    public AppConfigurationException(String message, Throwable e) {
        super(message, e);
    }
}
