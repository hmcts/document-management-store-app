package uk.gov.hmcts.dm.service;

public final class Constants {

    private Constants() {
    }

    public static final String IS_ADMIN = "isadmin";

    public static final Boolean FALSE = false;

    public static final String CASE_REF_REGEX = "^\\d{16}$";

    public static final String INVALID_CASEREF_EXCEPTION_MESSAGE = "Unable to verify caseRef pattern";

    public static final String EMPTY_CASEREF_EXCEPTION_MESSAGE
        = "You need to populate all required parameters - caseRef";

}
