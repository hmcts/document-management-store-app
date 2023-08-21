package uk.gov.hmcts.dm.errorhandler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.request.WebRequest;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;


@RunWith(SpringRunner.class)
public class ApiErrorControllerTest {

    @Mock
    HttpServletRequest request;

    @Mock
    ApiErrorAttributes apiErrorAttributes;

    @Mock
    WebRequest webRequest;

    @Mock
    ErrorAttributeOptions errorAttributeOptions;

    @Test
    public void shouldReturnResponseEntityNoContent() {
        when(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)).thenReturn(204);
        ApiErrorController apiErrorController = new ApiErrorController(apiErrorAttributes);
        assertThat(apiErrorController.error(request), equalTo(new ResponseEntity<>(HttpStatus.NO_CONTENT)));
    }

    @Test
    public void shouldReturnResponseEntityInternalServerError() {
        Map<String, Object> body = apiErrorAttributes.getErrorAttributes(webRequest, errorAttributeOptions);
        when(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)).thenReturn(500);
        ApiErrorController apiErrorController = new ApiErrorController(apiErrorAttributes);
        assertThat(apiErrorController.error(request), equalTo(new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR)));
    }

}
