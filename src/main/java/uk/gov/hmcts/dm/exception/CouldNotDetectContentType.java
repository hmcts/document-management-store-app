package uk.gov.hmcts.dm.exception;

/**
 * Created by pawel on 03/10/2017.
 */
public class CouldNotDetectContentType extends Exception  {

    public CouldNotDetectContentType(String expected, Throwable e) {
        super(String.format("Could not determine content type for expected %s", expected), e);
    }
    

}
