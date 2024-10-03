package uk.gov.hmcts.dm.errorhandler;

import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.context.MessageSource;
import org.springframework.core.MethodParameter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.FieldError;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
public class ExceptionStatusCodeAndMessageResolverTest {

    @Mock
    MessageSource messageSource;

    @InjectMocks
    private ExceptionStatusCodeAndMessageResolver resolver;

    @Before
    public void setUp() {
        resolver.init();
    }

    @Test
    public void should_throw_404_when_MethodArgumentTypeMismatchException_thrown() throws Exception {
        final MethodArgumentTypeMismatchException exception =
                new MethodArgumentTypeMismatchException(new HashMap<String, String>(), Map.class,
                        "test", new MethodParameter(Object.class.getMethod("toString"), -1), null);

        final ErrorStatusCodeAndMessage errorStatusCodeAndMessage =
                resolver.resolveStatusCodeAndMessage(exception, "It broke", 500, null);

        assertThat(errorStatusCodeAndMessage.getStatusCode(), equalTo(404));
    }

    @Test
    public void should_find_cause_from_exception_and_return_appropriate_code() {
        final FileSizeLimitExceededException fileSizeLimitExceededException =
                new FileSizeLimitExceededException("Too Big", 1234, 1024);
        final MultipartException multipartException =
                new MultipartException("Limit exceeded", fileSizeLimitExceededException);

        final ErrorStatusCodeAndMessage statusCodeAndMessage =
                resolver.resolveStatusCodeAndMessage(multipartException, "It broke", 500, null);

        assertThat(statusCodeAndMessage.getStatusCode(), equalTo(413));
    }

    @Test
    public void should_return_default_code_when_exception_not_in_map() {
        final int defaultStatusCode = 500;
        final String message = "It broke";
        final ErrorStatusCodeAndMessage errorStatusCodeAndMessage =
                resolver.resolveStatusCodeAndMessage(new RuntimeException("Test"), message, defaultStatusCode, null);

        assertThat(errorStatusCodeAndMessage.getStatusCode(), equalTo(defaultStatusCode));
    }

    @Test
    public void should_return_validation_message_instead_of_override_if_present() {
        FieldError fieldError1 = Mockito.mock(FieldError.class);
        FieldError fieldError2 = Mockito.mock(FieldError.class);

        Mockito.when(messageSource.getMessage(fieldError1, Locale.UK)).thenReturn("The validation message");
        Mockito.when(messageSource.getMessage(fieldError2, Locale.UK)).thenReturn("The validation message 2");

        final ErrorStatusCodeAndMessage errorStatusCodeAndMessage =
            resolver.resolveStatusCodeAndMessage(new RuntimeException("Test"),
                "x",
                100,
                Stream.of(fieldError1, fieldError2).toList());


        assertEquals("The validation message AND The validation message 2", errorStatusCodeAndMessage.getMessage());
    }
}
