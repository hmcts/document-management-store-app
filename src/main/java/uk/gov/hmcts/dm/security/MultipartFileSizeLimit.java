package uk.gov.hmcts.dm.security;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MultipartFileSizeValidator.class)
public @interface MultipartFileSizeLimit {
    String message() default "{uk.gov.hmcts.dm.security.MultipartFileListWhiteList.message}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
