package uk.gov.hmcts.dm.controller.provider;


import org.springframework.context.MessageSourceResolvable;
import org.springframework.hateoas.mediatype.MessageResolver;

public class NoOpMessageResolver implements MessageResolver {
    @Override
    public String resolve(MessageSourceResolvable resolvable) {
        return resolvable.getDefaultMessage() != null
            ? resolvable.getDefaultMessage()
            : String.join(",", resolvable.getCodes());
    }
}
