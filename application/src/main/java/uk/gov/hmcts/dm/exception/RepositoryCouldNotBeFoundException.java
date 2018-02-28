package uk.gov.hmcts.dm.exception;

/**
 * Created by pawel on 03/10/2017.
 */
public class RepositoryCouldNotBeFoundException extends RuntimeException  {

    public RepositoryCouldNotBeFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public RepositoryCouldNotBeFoundException(String message) {
        super(message);
    }
}
