package uk.gov.hmcts.dm.errorhandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.gov.hmcts.dm.config.logging.AppInsights;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(RestExceptionHandler.class);

    private final AppInsights appInsights;

    @Autowired
    public RestExceptionHandler(AppInsights appInsights) {
        this.appInsights = appInsights;
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    public ResponseEntity<Map<String, String>> handleAccessDeniedException(final HttpServletRequest request,
                                                                           final Exception exception) {
        trackException(exception);

        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .contentType(MediaType.APPLICATION_JSON)
            .body(bodyFromException(exception));
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<Map<String, String>> handleException(final HttpServletRequest request,
                                                               final Exception exception) {
        trackException(exception);

        return ResponseEntity
            .status(getHttpStatus(exception))
            .contentType(MediaType.APPLICATION_JSON)
            .body(bodyFromException(exception));
    }

    private void trackException(Exception exception) {
        LOG.error(exception.getMessage(), exception);
        appInsights.trackException(exception);
    }

    private Map<String, String> bodyFromException(Exception exception) {
        final Map<String, String> body = new HashMap<>();
        body.put("error", exception.getMessage());
        return body;
    }

    private HttpStatus getHttpStatus(Exception exception) {
        final ResponseStatus responseStatus = exception.getClass().getAnnotation(ResponseStatus.class);

        return null != responseStatus ? responseStatus.value() : HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
