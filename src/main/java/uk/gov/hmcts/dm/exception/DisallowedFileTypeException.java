package uk.gov.hmcts.dm.exception;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.UNPROCESSABLE_ENTITY)
public class DisallowedFileTypeException extends MethodArgumentNotValidException {

    /**
     * Constructor for {@link DisallowedFileTypeException}.
     * @param parameter the parameter that failed validation
     * @param bindingResult the results of the validation
     */
    public DisallowedFileTypeException(MethodParameter parameter, BindingResult bindingResult) {
        super(parameter, bindingResult);
    }
}
