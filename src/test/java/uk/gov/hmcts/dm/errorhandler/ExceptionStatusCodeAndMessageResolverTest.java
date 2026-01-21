package uk.gov.hmcts.dm.errorhandler;

import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.core.MethodParameter;
import org.springframework.validation.FieldError;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExceptionStatusCodeAndMessageResolverTest {

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private ExceptionStatusCodeAndMessageResolver resolver;

    @BeforeEach
    void setUp() {
        resolver.init();
    }

    @Test
    void shouldReturn404WhenMethodArgumentTypeMismatchExceptionThrown() throws NoSuchMethodException {
        MethodParameter methodParam = new MethodParameter(Object.class.getMethod("toString"), -1);

        MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException(
            new HashMap<String, String>(),
            Map.class,
            "test",
            methodParam,
            null
        );

        ErrorStatusCodeAndMessage result = resolver.resolveStatusCodeAndMessage(
            exception,
            "Default Message",
            500,
            null
        );

        assertEquals(404, result.getStatusCode());
        assertEquals(exception.getLocalizedMessage(), result.getMessage());
    }

    @Test
    void shouldFindCauseFromExceptionAndReturnAppropriateCode() {
        FileSizeLimitExceededException innerException =
            new FileSizeLimitExceededException("Too Big", 1234, 1024);

        MultipartException outerException =
            new MultipartException("Limit exceeded", innerException);

        ErrorStatusCodeAndMessage result = resolver.resolveStatusCodeAndMessage(
            outerException,
            "Default Message",
            500,
            null
        );

        assertEquals(413, result.getStatusCode());
    }

    @Test
    void shouldReturnDefaultCodeWhenExceptionNotInMap() {
        int defaultStatusCode = 500;
        String defaultMessage = "It broke";
        RuntimeException exception = new RuntimeException("Test Exception");

        ErrorStatusCodeAndMessage result = resolver.resolveStatusCodeAndMessage(
            exception,
            defaultMessage,
            defaultStatusCode,
            null
        );

        assertEquals(defaultStatusCode, result.getStatusCode());
        assertEquals("Test Exception", result.getMessage());
    }

    @Test
    void shouldReturnValidationMessageInsteadOfOverrideIfFieldErrorsPresent() {
        FieldError fieldError1 = mock(FieldError.class);
        FieldError fieldError2 = mock(FieldError.class);

        when(messageSource.getMessage(fieldError1, Locale.UK)).thenReturn("Validation error 1");
        when(messageSource.getMessage(fieldError2, Locale.UK)).thenReturn("Validation error 2");

        ErrorStatusCodeAndMessage result = resolver.resolveStatusCodeAndMessage(
            new RuntimeException("Test"),
            "Default Message",
            100,
            List.of(fieldError1, fieldError2)
        );

        assertEquals("Validation error 1 AND Validation error 2", result.getMessage());
    }
}
