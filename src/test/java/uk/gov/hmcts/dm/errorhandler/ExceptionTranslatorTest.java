package uk.gov.hmcts.dm.errorhandler;

import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import uk.gov.hmcts.dm.exception.ResourceNotFoundException;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(MockitoExtension.class)
class ExceptionTranslatorTest {

    @Mock
    private MessageSource messageSource;

    @Mock
    private WebRequest webRequest;

    @InjectMocks
    private ExceptionTranslator exceptionTranslator;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void shouldHandleResourceNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException(UUID.randomUUID());
        ResponseEntity<Object> response = exceptionTranslator.handleResourceNotFoundException(ex, webRequest);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void shouldHandleFileSizeLimitExceededException() {
        FileSizeLimitExceededException ex = new FileSizeLimitExceededException("File size limit exceeded", 0, 0);
        ResponseEntity<Object> response = exceptionTranslator.handleFileSizeLimitExceededException(ex, webRequest);
        assertThat(response.getStatusCode(), equalTo(HttpStatusCode.valueOf(413)));
    }

    @Test
    void shouldHandleSizeLimitExceededException() {
        SizeLimitExceededException ex = new SizeLimitExceededException("Size limit exceeded", 0, 0);
        ResponseEntity<Object> response = exceptionTranslator.handleSizeLimitExceededException(ex, webRequest);
        assertThat(response.getStatusCode(), equalTo(HttpStatusCode.valueOf(413)));
    }

    @Test
    void shouldHandleMethodArgumentTypeMismatchException() {
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(null, null, null, null, null);
        ResponseEntity<Object> response = exceptionTranslator.handleMethodArgumentTypeMismatchException(ex, webRequest);
        assertThat(response.getStatusCode(), equalTo(HttpStatusCode.valueOf(400)));
    }

    @Test
    void shouldHandleMethodArgumentNotValidExceptionWithFieldErrors() {
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(
            new MethodParameter(ExceptionTranslator.class.getMethods()[0], -1),
            new BeanPropertyBindingResult(new Object(), "objectName")
        );
        ResponseEntity<Object> response = exceptionTranslator.handleMethodArgumentNotValid(ex, new HttpHeaders(),
            HttpStatusCode.valueOf(422), webRequest);
        assertThat(response.getStatusCode(), equalTo(HttpStatusCode.valueOf(422)));
    }

    @Test
    void shouldHandleExceptionInternalWithNullBody() {
        Exception ex = new Exception("Internal error");
        ResponseEntity<Object> response = exceptionTranslator.handleExceptionInternal(ex, null,
            new HttpHeaders(), HttpStatusCode.valueOf(500), webRequest);
        assertThat(response.getStatusCode(), equalTo(HttpStatusCode.valueOf(500)));
    }

    @Test
    void shouldHandleExceptionInternalWithNonNullBody() {
        Exception ex = new Exception("Internal error");
        Object body = new Object();
        ResponseEntity<Object> response = exceptionTranslator.handleExceptionInternal(ex, body,
            new HttpHeaders(), HttpStatusCode.valueOf(500), webRequest);
        assertThat(response.getStatusCode(), equalTo(HttpStatusCode.valueOf(500)));
    }
}
