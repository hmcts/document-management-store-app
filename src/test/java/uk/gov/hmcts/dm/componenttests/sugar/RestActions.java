package uk.gov.hmcts.dm.componenttests.sugar;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.exception.DMRuntimeException;
import uk.gov.hmcts.reform.auth.checker.core.service.ServiceRequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.core.user.UserRequestAuthorizer;
import uk.gov.hmcts.dm.componenttests.backdoors.ServiceResolverBackdoor;
import uk.gov.hmcts.dm.componenttests.backdoors.UserResolverBackdoor;
import uk.gov.hmcts.dm.security.Classifications;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON;

public class RestActions {
    private final HttpHeaders httpHeaders = new HttpHeaders();
    private final MockMvc mvc;
    private final ServiceResolverBackdoor serviceRequestAuthorizer;
    private final UserResolverBackdoor userRequestAuthorizer;
    private final ObjectMapper objectMapper;

    public RestActions(MockMvc mvc, ServiceResolverBackdoor serviceRequestAuthorizer, UserResolverBackdoor userRequestAuthorizer, ObjectMapper objectMapper) {
        this.mvc = mvc;
        this.serviceRequestAuthorizer = serviceRequestAuthorizer;
        this.userRequestAuthorizer = userRequestAuthorizer;
        this.objectMapper = objectMapper;
    }

    public RestActions withAuthorizedService(String serviceId) {
        String token = UUID.randomUUID().toString();
        serviceRequestAuthorizer.registerToken(token, serviceId);
        httpHeaders.add(ServiceRequestAuthorizer.AUTHORISATION, token);
        return this;
    }

    public RestActions withAuthorizedUser(String userId) {
        String token = UUID.randomUUID().toString();
        userRequestAuthorizer.registerToken(token, userId);
        httpHeaders.add(UserRequestAuthorizer.AUTHORISATION, token);
        return this;
    }

    public ResultActions get(String urlTemplate) {
        return translateException(() -> mvc.perform(MockMvcRequestBuilders.get(urlTemplate)
            .headers(httpHeaders))
        );
    }

    public ResultActions delete(String urlTemplate) {
        return translateException(() -> mvc.perform(MockMvcRequestBuilders.delete(urlTemplate)
                .headers(httpHeaders))
        );
    }

    public ResultActions post(String urlTemplate) {
        return post(urlTemplate, null);
    }

    public ResultActions post(String urlTemplate, Object requestBody) {
        return translateException(() -> mvc.perform(MockMvcRequestBuilders.post(urlTemplate)
            .headers(httpHeaders)
            .content(toJson(requestBody))
            .contentType(APPLICATION_JSON)));
    }

    public ResultActions put(String urlTemplate, Object requestBody) {
        return translateException(() -> mvc.perform(MockMvcRequestBuilders.put(urlTemplate)
                .headers(httpHeaders)
                .content(toJson(requestBody))
                .contentType(APPLICATION_JSON)));
    }

    public ResultActions postDocuments(String urlTemplate, List<MultipartFile> files, Classifications classification, List<String> roles) {

        MockMultipartHttpServletRequestBuilder builder = MockMvcRequestBuilders.fileUpload(urlTemplate);

        builder.param("classification", classification.toString());

        files.forEach( f -> builder.file((MockMultipartFile) f) );

        return translateException(() -> mvc.perform(
            builder.headers(httpHeaders)
        ));
    }

    public ResultActions postDocument(String urlTemplate, MultipartFile file) {
        return translateException(() -> mvc.perform(
                    MockMvcRequestBuilders.fileUpload(urlTemplate).file((MockMultipartFile)file)
                        .headers(httpHeaders)
                ));
    }

    public ResultActions postDocumentVersion(String urlTemplate, MockMultipartFile file) {

        MockMultipartHttpServletRequestBuilder builder =
                MockMvcRequestBuilders.fileUpload(urlTemplate);


        builder.with(request -> {
            request.setMethod("POST");
            return request;
        });

        return translateException(() -> mvc.perform(
                builder
                    .file(file)
                    .headers(httpHeaders)
        ));
    }

    private String toJson(Object o) {
        return translateException(() -> objectMapper.writeValueAsString(o));
    }

    private <T> T translateException(CallableWithException<T> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            throw new DMRuntimeException(e);
        }
    }

    interface CallableWithException<T> {
        T call() throws Exception;
    }
}
