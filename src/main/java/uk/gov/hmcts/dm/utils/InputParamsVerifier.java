package uk.gov.hmcts.dm.utils;

import uk.gov.hmcts.dm.commandobject.DeleteCaseDocumentsCommand;
import uk.gov.hmcts.dm.exception.InvalidRequestException;
import uk.gov.hmcts.dm.service.Constants;

import static java.util.regex.Pattern.compile;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

public final class InputParamsVerifier {

    private InputParamsVerifier() {
    }

    public static void verifyRequestParamsAreNotEmpty(final DeleteCaseDocumentsCommand deleteCaseDocumentsCommand)
        throws InvalidRequestException {
        if (isEmpty(deleteCaseDocumentsCommand.getCaseRef())) {
            throw new InvalidRequestException(Constants.EMPTY_CASEREF_EXCEPTION_MESSAGE, BAD_REQUEST);
        }
    }

    public static void verifyRequestParamsConditions(final DeleteCaseDocumentsCommand deleteCaseDocumentsCommand)
        throws InvalidRequestException {
        verifyCaseRef(deleteCaseDocumentsCommand.getCaseRef(), Constants.INVALID_CASEREF_EXCEPTION_MESSAGE);
    }

    private static void verifyCaseRef(final String caseRef,
                                      final String exceptionMessage) throws InvalidRequestException {
        if (!isEmpty(caseRef)
            && !compile(Constants.CASE_REF_REGEX).matcher(caseRef).matches()) {
            throw new InvalidRequestException(exceptionMessage, BAD_REQUEST);
        }
    }

}
