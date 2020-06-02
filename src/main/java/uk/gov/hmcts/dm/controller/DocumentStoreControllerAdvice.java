package uk.gov.hmcts.dm.controller;

import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Map;

@ControllerAdvice()
public class DocumentStoreControllerAdvice extends ResponseEntityExceptionHandler {

    @Autowired
    private ErrorAttributes errorAttributes;

    @Value("${errors.globalIncludeStackTrace}")
    private boolean globalIncludeStackTrace = true;

    @ExceptionHandler({
        FileSizeLimitExceededException.class,
        SizeLimitExceededException.class,
        MethodArgumentTypeMismatchException.class
    })
    @ResponseBody
    ResponseEntity<?> handleControllerException(HttpServletRequest request, Throwable ex) {
        WebRequest webRequest = new ServletWebRequest(request);
        Map<String, Object> body = errorAttributes.getErrorAttributes(webRequest, getOptions());

        return new ResponseEntity<>(body, getStatus(webRequest));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        Map<String, Object> body = errorAttributes.getErrorAttributes(request, getOptions());

        return handleExceptionInternal(ex, body, headers, getStatus(request), request);
    }

    private HttpStatus getStatus(WebRequest webRequest) {
        Integer statusCode = (Integer) webRequest.getAttribute(RequestDispatcher.ERROR_STATUS_CODE, 0);
        if (statusCode == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return HttpStatus.valueOf(statusCode);
    }

    private ErrorAttributeOptions getOptions() {
        ArrayList<ErrorAttributeOptions.Include> includes = new ArrayList<ErrorAttributeOptions.Include>();
        if (globalIncludeStackTrace) {
            includes.add(ErrorAttributeOptions.Include.STACK_TRACE);
        }
        return ErrorAttributeOptions.of(includes);
    }
}
