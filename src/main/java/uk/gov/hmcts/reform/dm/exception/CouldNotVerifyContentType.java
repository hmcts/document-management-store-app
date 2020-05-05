package uk.gov.hmcts.reform.dm.exception;

public class CouldNotVerifyContentType extends Exception  {

    public CouldNotVerifyContentType(String expected, Throwable e) {
        super(String.format("Could not determine content type for expected %s", expected), e);
    }


}
