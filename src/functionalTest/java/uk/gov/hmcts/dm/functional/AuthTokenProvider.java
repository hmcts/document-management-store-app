package uk.gov.hmcts.dm.functional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.hamcrest.CoreMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Service
public class AuthTokenProvider {

    private final String idamS2SBaseUri;
    private final String idamUserBaseUrl;
    private final String s2sSecret;
    private final String ccdCaseDisposerS2sSecret;

    @Autowired
    public AuthTokenProvider(@Value("${base-urls.idam-s2s}") String idamS2SBaseUri,
                             @Value("${base-urls.idam-user}") String idamUserBaseUri,
                             @Value("${base-urls.s2s-token}") String s2sSecret,
                             @Value("${base-urls.ccd-case-disposer-s2s-token}") String ccdCaseDisposerS2sSecret) {
        this.idamS2SBaseUri = idamS2SBaseUri;
        this.idamUserBaseUrl = idamUserBaseUri;
        this.s2sSecret = s2sSecret;
        this.ccdCaseDisposerS2sSecret = ccdCaseDisposerS2sSecret;
        System.out.println("IDAM User URL - " + idamUserBaseUri);
        System.out.println("IDAM S2S URL - " + idamS2SBaseUri);
    }

    public AuthTokens getTokens(String email, String password) {
        String userToken = findUserToken(email, password);
        return new AuthTokens(userToken, "");
    }

    public void createIdamUser(String email, String password, Optional<String> maybeRole) throws JsonProcessingException {
        ImmutableMap<String, Object> body = ImmutableMap.of("email", email,
                "forename", "test",
                "surname", "test",
                "password", password,
                "roles", ImmutableList.of(ImmutableMap.of("code", maybeRole.orElse("citizen"),
                        "displayName", maybeRole.orElse("Citizen"))));

        RestAssured
                .given().log().all().baseUri(idamUserBaseUrl)
                .body(new ObjectMapper().writeValueAsBytes(body))
                .contentType("application/json")
                .post("testing-support/accounts")
                .then()
                .statusCode(204);
    }

    public String findServiceToken() {
        Map<String, Object> params = ImmutableMap.of(
            "microservice", "em_gw",
            "oneTimePassword", new GoogleAuthenticator().getTotpPassword(this.s2sSecret)
        );

        Response response = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(this.idamS2SBaseUri)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .body(params)
            .post("/lease")
            .andReturn();

        assertThat(response.getStatusCode(), CoreMatchers.equalTo(200));

        return "Bearer " + response
            .getBody()
            .print();
    }

    public String findCcdCaseDisposerServiceToken() {
        Map<String, Object> params = ImmutableMap.of(
            "microservice", "ccd_case_disposer",
            "oneTimePassword", new GoogleAuthenticator().getTotpPassword(this.ccdCaseDisposerS2sSecret)
        );

        Response response = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(this.idamS2SBaseUri)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .body(params)
            .post("/lease")
            .andReturn();

        assertThat(response.getStatusCode(), CoreMatchers.equalTo(200));

        return "Bearer " + response
            .getBody()
            .print();
    }

    private String findUserToken(String email, String password) {
        final String encoded = Base64.getEncoder().encodeToString((email + ":" + password).getBytes());
        final Response authorization = RestAssured.given().baseUri(idamUserBaseUrl)
                .header("Authorization", "Basic " + encoded)
                .post("oauth2/authorize");
        authorization.then().statusCode(200);
        final Map<String, String> userResponse = authorization.andReturn().as(Map.class);
        return userResponse.get("access-token");
    }

    public int findUserId(String userToken) {
        final Response details =
            RestAssured.given()
            .baseUri(idamUserBaseUrl)
            .header("Authorization", "Bearer " + userToken)
            .get("details");

        details.then().statusCode(200);
        return details.path("id");
    }

    public void deleteUser(String probateCaseworkerEmail) {
        RestAssured.given().log().all().baseUri(idamUserBaseUrl)
                .delete("testing-support/accounts/" + probateCaseworkerEmail)
                .andReturn()
                .getStatusCode();
    }

    public class AuthTokens {
        private final String userToken;
        private final String serviceToken;

        public AuthTokens(String userToken, String serviceToken) {
            this.userToken = userToken;
            this.serviceToken = serviceToken;
        }

        public String getUserToken() {
            return userToken;
        }

        public String getServiceToken() {
            return serviceToken;
        }

        @Override
        public String toString() {
            return "AuthTokens{"
                + "userToken='" + userToken + '\''
                + ", serviceToken='" + serviceToken + '\''
                + '}';
        }
    }
}
