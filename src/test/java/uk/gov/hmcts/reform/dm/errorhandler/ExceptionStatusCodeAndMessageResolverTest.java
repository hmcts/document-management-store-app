package uk.gov.hmcts.reform.dm.errorhandler;

import org.apache.tomcat.util.http.fileupload.FileUploadBase;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.MethodParameter;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

public class ExceptionStatusCodeAndMessageResolverTest {

    private ExceptionStatusCodeAndMessageResolver resolver = new ExceptionStatusCodeAndMessageResolver();

    @Before
    public void setUp() throws Exception {
        resolver.init();
    }

    @Test
    public void shouldThrow404WhenMethodArgumentTypeMismatchExceptionThrown() throws Exception {
        final MethodArgumentTypeMismatchException exception =
                new MethodArgumentTypeMismatchException(new HashMap<String, String>(), Map.class,
                        "test", new MethodParameter(Object.class.getMethod("toString"), 1), null);

        final ErrorStatusCodeAndMessage errorStatusCodeAndMessage =
                resolver.resolveStatusCodeAndMessage(exception, "It broke", 500);

        assertThat(errorStatusCodeAndMessage.getStatusCode(), equalTo(404));
    }

    @Test
    public void shouldFindCauseFromExceptionAndReturnAppropriateCode() throws Exception {
        final FileUploadBase.FileSizeLimitExceededException fileSizeLimitExceededException =
                new FileUploadBase.FileSizeLimitExceededException("Too Big", 1234, 1024);
        final MultipartException multipartException =
                new MultipartException("Limit exceeded", fileSizeLimitExceededException);

        final ErrorStatusCodeAndMessage statusCodeAndMessage =
                resolver.resolveStatusCodeAndMessage(multipartException, "It broke", 500);

        assertThat(statusCodeAndMessage.getStatusCode(), equalTo(413));
    }

    @Test
    public void shouldReturnDefaultCodeWhenExceptionNotInMap() throws Exception {
        final int defaultStatusCode = 500;
        final String message = "It broke";
        final ErrorStatusCodeAndMessage errorStatusCodeAndMessage =
                resolver.resolveStatusCodeAndMessage(new RuntimeException("Test"), message, defaultStatusCode);

        assertThat(errorStatusCodeAndMessage.getStatusCode(), equalTo(defaultStatusCode));
    }
}
