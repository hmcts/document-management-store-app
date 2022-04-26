package uk.gov.hmcts.dm.errorhandler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;


@RunWith(SpringRunner.class)
public class ApiErrorControllerTest {

    @Mock
    HttpServletRequest request;

    @Mock
    ApiErrorAttributes apiErrorAttributes;

    @Test
    public void shouldReturnResponseEntityNoContent() {
        when(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)).thenReturn(204);
        ApiErrorController apiErrorController = new ApiErrorController(apiErrorAttributes);
        assertThat(apiErrorController.error(request), equalTo(new ResponseEntity<>(HttpStatus.NO_CONTENT)));
    }

    @Test
    public void shouldReturnResponseEntityInternalServerError() {
        when(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)).thenReturn(500);
        ApiErrorController apiErrorController = new ApiErrorController(apiErrorAttributes);
        assertThat(apiErrorController.error(request), equalTo(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR)));
    }

}
