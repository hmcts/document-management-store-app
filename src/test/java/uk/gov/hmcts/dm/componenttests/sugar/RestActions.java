package uk.gov.hmcts.dm.componenttests.sugar;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.security.Classifications;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.doReturn;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.dm.service.SecurityUtilService.USER_ID_HEADER;

public class RestActions {
    private final HttpHeaders httpHeaders = new HttpHeaders();
    private final MockMvc mvc;
    private final ObjectMapper objectMapper;

    @Mock
    protected Authentication authentication;

    @Mock
    protected SecurityContext securityContext;

    public RestActions(WebApplicationContext webApplicationContext, ObjectMapper objectMapper) {
        MockitoAnnotations.openMocks(this);
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);
        this.mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        this.objectMapper = objectMapper;
    }

    public RestActions withAuthorizedUser(String userId) {
        httpHeaders.add(USER_ID_HEADER, userId);
        return this;
    }

    public RestActions withHeader(String key, String value) {
        httpHeaders.add(key, value);
        return this;
    }

    public ResultActions get(String urlTemplate) {
        return translateException(() -> mvc.perform(MockMvcRequestBuilders.get(urlTemplate)
            .headers(httpHeaders)
            .principal(new UsernamePasswordAuthenticationToken(UUID.randomUUID().toString(), null)))
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

    public ResultActions patch(String urlTemplate, Object requestBody) {

        return translateException(() -> mvc.perform(MockMvcRequestBuilders.patch(urlTemplate)
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

    //TODO: decide what to do with roles
    public ResultActions postDocuments(String urlTemplate, List<MultipartFile> files,
                                       Classifications classification, List<String> roles) {

        MockMultipartHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart(urlTemplate);

        builder.param("classification", classification.toString());

        files.forEach(f -> builder.file((MockMultipartFile) f));

        return translateException(() -> mvc.perform(
            builder.headers(httpHeaders)
        ));

    }

    public ResultActions postDocument(String urlTemplate, MultipartFile file) {
        return translateException(() -> mvc.perform(
                    MockMvcRequestBuilders.multipart(urlTemplate).file((MockMultipartFile)file)
                        .headers(httpHeaders)
                ));

    }

    public ResultActions postDocumentVersion(String urlTemplate, MockMultipartFile file) {

        MockMultipartHttpServletRequestBuilder builder =
                MockMvcRequestBuilders.multipart(urlTemplate);


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
            throw new RuntimeException(e);
        }
    }

    interface CallableWithException<T> {
        T call() throws Exception;
    }
}
