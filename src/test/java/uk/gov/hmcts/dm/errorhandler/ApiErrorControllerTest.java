package uk.gov.hmcts.dm.errorhandler;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiErrorControllerTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private ErrorAttributes errorAttributes;

    @Test
    void shouldReturnResponseEntityNoContent() {
        ApiErrorController apiErrorController = new ApiErrorController(errorAttributes);
        when(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)).thenReturn(204);

        ResponseEntity<Map<String, Object>> response = apiErrorController.error(request);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void shouldReturnResponseEntityInternalServerError() {
        ApiErrorController apiErrorController = new ApiErrorController(errorAttributes);
        when(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)).thenReturn(500);

        ResponseEntity<Map<String, Object>> response = apiErrorController.error(request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        assertEquals(Collections.emptyMap(), response.getBody());
    }
}
