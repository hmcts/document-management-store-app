package uk.gov.hmcts.dm.errorhandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.web.context.request.RequestAttributes;

import java.util.List;
import java.util.Map;

@Component
public class ApiErrorAttributes extends DefaultErrorAttributes {

    private static final Logger log = LoggerFactory.getLogger(ApiErrorAttributes.class);

    @Value("${errors.globalIncludeStackTrace}")
    private boolean globalIncludeStackTrace = true;

    @Autowired
    private ExceptionStatusCodeAndMessageResolver exceptionStatusCodeAndMessageResolver;

    @Override
    public Map<String, Object> getErrorAttributes(
            RequestAttributes requestAttributes,
            boolean includeStackTrace) {

        Map<String, Object> errorAttributes = super.getErrorAttributes(requestAttributes, true);

        List<FieldError> errors = (List<FieldError>)errorAttributes.remove("errors");

        Throwable throwable = getError(requestAttributes);

        ErrorStatusCodeAndMessage errorStatusCodeAndMessage = exceptionStatusCodeAndMessageResolver
            .resolveStatusCodeAndMessage(
                throwable,
                (String) errorAttributes.get("message"),
                (Integer) requestAttributes.getAttribute("javax.servlet.error.status_code", 0),
                errors);

        errorAttributes.put("error", errorStatusCodeAndMessage.getMessage());
        requestAttributes.setAttribute("javax.servlet.error.status_code", errorStatusCodeAndMessage.getStatusCode(), 0);
        errorAttributes.put("status", errorStatusCodeAndMessage.getStatusCode());
        if (throwable != null) {
            log.error(throwable.getMessage(), throwable);
        }

        if (!globalIncludeStackTrace) {
            errorAttributes.remove("exception");
            errorAttributes.remove("trace");
        }

        errorAttributes.remove("message");

        return errorAttributes;
    }

}
