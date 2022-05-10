package uk.gov.hmcts.dm.service;

public interface Constants {

    String IS_ADMIN = "isadmin";

    Boolean FALSE = false;

    String CASE_REF_REGEX = "^\\d{16}$";

    String INVALID_CASEREF_EXCEPTION_MESSAGE = "Unable to verify caseRef pattern";

    String EMPTY_CASEREF_EXCEPTION_MESSAGE = "You need to populate all required parameters - caseRef";

}
