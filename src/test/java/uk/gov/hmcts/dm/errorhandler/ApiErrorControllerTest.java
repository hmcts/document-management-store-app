package uk.gov.hmcts.dm.errorhandler;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;


@ExtendWith(SpringExtension.class)
class ApiErrorControllerTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private ApiErrorAttributes apiErrorAttributes;

    @Mock
    private WebRequest webRequest;

    @Mock
    private ErrorAttributeOptions errorAttributeOptions;

    @Test
    void shouldReturnResponseEntityNoContent() {
        when(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)).thenReturn(204);
        ApiErrorController apiErrorController = new ApiErrorController(apiErrorAttributes);
        assertThat(apiErrorController.error(request), equalTo(new ResponseEntity<>(HttpStatus.NO_CONTENT)));
    }

    @Test
    void shouldReturnResponseEntityInternalServerError() {
        Map<String, Object> body = apiErrorAttributes.getErrorAttributes(webRequest, errorAttributeOptions);
        when(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)).thenReturn(500);
        ApiErrorController apiErrorController = new ApiErrorController(apiErrorAttributes);
        assertThat(apiErrorController.error(request),
            equalTo(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON).body(body)));
    }

}
