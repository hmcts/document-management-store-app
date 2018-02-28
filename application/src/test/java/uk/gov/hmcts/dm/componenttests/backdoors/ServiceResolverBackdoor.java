package uk.gov.hmcts.dm.componenttests.backdoors;

import uk.gov.hmcts.reform.auth.checker.core.SubjectResolver;
import uk.gov.hmcts.reform.auth.checker.core.exceptions.AuthCheckerException;
import uk.gov.hmcts.reform.auth.checker.core.service.Service;

import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;

public class ServiceResolverBackdoor implements SubjectResolver<Service> {

    private final ConcurrentHashMap<String, String> tokenToServiceMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        registerToken("sscs", "sscs");
    }

    @Override
    public Service getTokenDetails(String token) {
        String serviceId = tokenToServiceMap.get(token);

        if (serviceId == null) {
            throw new AuthCheckerException("Token not found");
        }

        return new Service(serviceId);
    }

    public void registerToken(String token, String serviceId) {
        tokenToServiceMap.put(token, serviceId);
    }
}
