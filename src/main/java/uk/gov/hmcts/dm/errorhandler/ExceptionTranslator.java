package uk.gov.hmcts.dm.errorhandler;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.gov.hmcts.dm.exception.ResourceNotFoundException;

import java.util.Locale;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ExceptionTranslator extends ResponseEntityExceptionHandler {

    MessageSource messageSource;

    @Value("${errors.globalIncludeStackTrace}")
    private boolean globalIncludeStackTrace;

    @Autowired
    public ExceptionTranslator(MessageSource messageSource) {
        this.messageSource = messageSource;
    }


    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        return handleExceptionInternal(ex, null, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(FileSizeLimitExceededException.class)
    public ResponseEntity<Object> handleFileSizeLimitExceededException(FileSizeLimitExceededException ex,
                                                                       WebRequest request) {
        return handleExceptionInternal(ex, null, new HttpHeaders(), HttpStatusCode.valueOf(413), request);
    }

    @ExceptionHandler(SizeLimitExceededException.class)
    public ResponseEntity<Object> handleSizeLimitExceededException(SizeLimitExceededException ex,
                                                                       WebRequest request) {
        return handleExceptionInternal(ex, null, new HttpHeaders(), HttpStatusCode.valueOf(413), request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex,
                                                                       WebRequest request) {
        return handleExceptionInternal(ex, null, new HttpHeaders(), HttpStatusCode.valueOf(400), request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        if (ex.hasFieldErrors()) {
            String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> messageSource.getMessage(fieldError, Locale.UK))
                .collect(Collectors.joining(" AND "));
            ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, "Request validation failed");
            problemDetail.setProperty("error", errorMessage);
            return handleExceptionInternal(ex, problemDetail, headers, HttpStatusCode.valueOf(422), request);
        }
        return handleExceptionInternal(ex, null, new HttpHeaders(), HttpStatusCode.valueOf(422), request);
    }


    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
        Exception ex, @Nullable Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {

        if (responseCommitted(ex, request)) {
            return null;
        }

        if (body == null) {
            ProblemDetail problemDetail;
            if (ex instanceof ErrorResponse errorResponse) {
                if (statusCode.equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
                    statusCode = errorResponse.getStatusCode();
                }
                problemDetail = ProblemDetail.forStatusAndDetail(statusCode, errorResponse.getDetailMessageCode());
            } else {
                problemDetail = ProblemDetail.forStatusAndDetail(statusCode, ex.getLocalizedMessage());
            }

            String rootCauseMessage = ExceptionUtils.getRootCauseMessage(ex);
            if (globalIncludeStackTrace) {
                problemDetail.setProperty("exception", ExceptionUtils.getRootCause(ex).getClass().getName());
            }
            problemDetail.setProperty("error", rootCauseMessage.substring(rootCauseMessage.indexOf(":") + 2));
            body = problemDetail;
        }

        return ResponseEntity
            .status(statusCode)
            .headers(headers)
            .contentType(MediaType.APPLICATION_JSON)
            .body(body);
    }

    private boolean responseCommitted(Exception ex, WebRequest request) {
        if (request instanceof ServletWebRequest servletWebRequest) {
            HttpServletResponse response = servletWebRequest.getResponse();
            if (response != null && response.isCommitted()) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Response already committed. Ignoring: " + ex);
                }
                return true;
            }
        }
        return false;
    }

}
