package uk.gov.hmcts.dm.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import jakarta.servlet.http.HttpServletResponse;

@ResponseStatus(value = HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
public class InvalidRangeRequestException extends RuntimeException {

    public InvalidRangeRequestException(Exception e, HttpServletResponse response, long contentLength) {
        super(e);
        setRequiredResponseHeader(response, contentLength);
    }

    public InvalidRangeRequestException(HttpServletResponse response, long contentLength) {
        super();
        setRequiredResponseHeader(response, contentLength);
    }

    private void setRequiredResponseHeader(HttpServletResponse response, long contentLength) {
        response.setHeader(HttpHeaders.CONTENT_RANGE, "bytes */" + contentLength); // Required in 416.
    }

}
