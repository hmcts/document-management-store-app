package uk.gov.hmcts.dm.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import uk.gov.hmcts.dm.commandobject.DeleteCaseDocumentsCommand;
import uk.gov.hmcts.dm.exception.InvalidRequestException;
import uk.gov.hmcts.dm.service.Constants;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.fail;
import static uk.gov.hmcts.dm.utils.InputParamsVerifier.verifyRequestParamsAreNotEmpty;
import static uk.gov.hmcts.dm.utils.InputParamsVerifier.verifyRequestParamsConditions;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InputParamsVerifierTest {

    @Test
    void shouldVerifyRequestParamsAreNotEmptyForCaseRef() {
        assertDoesNotThrow(() -> verifyRequestParamsAreNotEmpty(new DeleteCaseDocumentsCommand("123")));
    }

    @Test
    void shouldThrowExceptionWhenRequestParamsAreEmptyForCaseRef() {
        try {
            verifyRequestParamsAreNotEmpty(new DeleteCaseDocumentsCommand(null));
            fail("The method should have thrown InvalidRequestException due to unpopulated caseRef");
        } catch (final InvalidRequestException invalidRequestException) {
            assertThat(invalidRequestException.getMessage())
                .isEqualTo(Constants.EMPTY_CASEREF_EXCEPTION_MESSAGE);
        }
    }

    @Test
    void shouldVerifyRequestParamsConditionsForCaseRef() {
        assertDoesNotThrow(() -> verifyRequestParamsConditions(new DeleteCaseDocumentsCommand(randomNumeric(16))));
    }

    @Test
    void shouldThrowExceptionWhenRequestParamsConditionsInvalidForCaseRef() {
        try {
            final DeleteCaseDocumentsCommand deleteCaseDocumentsCommand =
                new DeleteCaseDocumentsCommand(randomNumeric(17));
            verifyRequestParamsConditions(deleteCaseDocumentsCommand);
            fail("The method should have thrown InvalidRequestException due to invalid caseRef");
        } catch (final InvalidRequestException invalidRequestException) {
            assertThat(invalidRequestException.getMessage())
                .isEqualTo(Constants.INVALID_CASEREF_EXCEPTION_MESSAGE);
        }
    }
}
