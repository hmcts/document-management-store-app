package uk.gov.hmcts.dm.config.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.auth.checker.core.SubjectResolver;
import uk.gov.hmcts.reform.auth.checker.core.exceptions.UnauthorisedServiceException;
import uk.gov.hmcts.reform.auth.checker.core.service.Service;
import uk.gov.hmcts.reform.auth.checker.core.service.ServiceRequestAuthorizer;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.function.Function;

@Component
public class DmServiceRequestAuthorizer extends ServiceRequestAuthorizer {

    private final Logger logger = LoggerFactory.getLogger(DmServiceRequestAuthorizer.class);

    public DmServiceRequestAuthorizer(SubjectResolver<Service> serviceResolver, Function<HttpServletRequest, Collection<String>> authorizedServicesExtractor) {
        super(serviceResolver, authorizedServicesExtractor);
    }

    @Override
    public Service authorise(HttpServletRequest request) throws UnauthorisedServiceException {

        Service service = super.authorise(request);
        logger.info("dm-store : Endpoint : {}  for : {} method is accessed by {}",
            request.getRequestURI(), request.getMethod(), service.getPrincipal().toLowerCase());
        return service;
    }
}
