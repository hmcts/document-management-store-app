package uk.gov.hmcts.dm.errorhandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Component
public class ApiErrorController extends BasicErrorController {

    @Autowired
    public ApiErrorController(ErrorAttributes errorAttributes) {
        super(errorAttributes, new ErrorProperties());
    }

    @RequestMapping(method = RequestMethod.GET)
    @Override
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
        HttpStatus status = getStatus(request);
        if (status == HttpStatus.NO_CONTENT) {
            return new ResponseEntity<>(status);
        }
        Map<String, Object> body = getErrorAttributes(request, getErrorAttributeOptions(request, MediaType.ALL));
        // Set the status again for the legacy error handling code to work
        // This was changed by spring here: https://github.com/spring-projects/spring-boot/commit/a354657acef83919cb3685c7dd50f2a42156efca#r35879840
        // Clearly the error handling in this service is not correct but it's complex enough to take a long time
        // to replicate the behaviour doing it 'the right way'
        // For the sake of time, I've overridden this method to achieve the same functionality as before and raised
        // a Jira ticket as a tech debt item to resolve this in the future.
        status = getStatus(request);
        return new ResponseEntity<>(body, status);
    }
}
