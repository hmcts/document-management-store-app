package uk.gov.hmcts.dm.errorhandler;

import org.apache.tomcat.util.http.fileupload.FileUploadBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ExceptionStatusCodeAndMessageResolver {

    private Map<Class<? extends Throwable>, Integer> exceptionToStatusCodeMap = new HashMap<>();

    private Map<Class<? extends Throwable>, String> exceptionToMessageMap = new HashMap<>();

    @Autowired
    private MessageSource messageSource;

    @PostConstruct
    public void init() {
        exceptionToStatusCodeMap.put(FileUploadBase.FileSizeLimitExceededException.class, 413);
        exceptionToStatusCodeMap.put(FileUploadBase.SizeLimitExceededException.class, 413);
        exceptionToStatusCodeMap.put(MethodArgumentTypeMismatchException.class, 404);
        exceptionToStatusCodeMap.put(MethodArgumentNotValidException.class, 422);

        exceptionToMessageMap.put(MethodArgumentNotValidException.class, "Request validation failed");
    }

    public ErrorStatusCodeAndMessage resolveStatusCodeAndMessage(
            Throwable throwable,
            String defaultMessage,
            Integer defaultStatusCode,
            List<FieldError> fieldErrorList) {

        ErrorStatusCodeAndMessage errorStatusCodeAndMessage =
                new ErrorStatusCodeAndMessage(defaultMessage, defaultStatusCode);

        if (throwable != null) {
            Throwable ultimateCause = findUltimateCause(throwable);
            if (isThrowableStatusCodeOverridden(ultimateCause)) {
                errorStatusCodeAndMessage.setStatusCode(exceptionToStatusCodeMap.get(ultimateCause.getClass()));
            }
            if (isThrowableMessageOverridden(ultimateCause)) {
                errorStatusCodeAndMessage.setMessage(exceptionToMessageMap.get(ultimateCause.getClass()));
            } else {
                errorStatusCodeAndMessage.setMessage(ultimateCause.getLocalizedMessage());
            }
        }

        if (fieldErrorList != null) {
            errorStatusCodeAndMessage.setMessage(
                fieldErrorList.stream()
                    .map(fieldError -> messageSource.getMessage(fieldError, Locale.UK))
                    .collect(Collectors.joining(" AND ")));
        }

        return errorStatusCodeAndMessage;

    }

    private boolean isThrowableStatusCodeOverridden(Throwable throwable) {
        return exceptionToStatusCodeMap.containsKey(throwable.getClass());
    }

    private boolean isThrowableMessageOverridden(Throwable throwable) {
        return exceptionToMessageMap.containsKey(throwable.getClass());
    }

    private Throwable findUltimateCause(Throwable throwable) {
        Throwable t = throwable;
        boolean found = false;
        while (!found) {
            if (t.getCause() == null) {
                found = true;
            } else {
                if (isThrowableStatusCodeOverridden(t) || isThrowableMessageOverridden(t)) {
                    found = true;
                } else {
                    t = t.getCause();
                }
            }
        }
        return t;
    }


}
