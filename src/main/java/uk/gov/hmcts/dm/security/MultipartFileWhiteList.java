package uk.gov.hmcts.dm.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MultipartFileWhiteListValidator.class)
public @interface MultipartFileWhiteList {
    String message() default "{uk.gov.hmcts.dm.security.MultipartFileListWhiteList.message}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
