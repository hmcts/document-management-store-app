package uk.gov.hmcts.dm.it;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthTokenProvider {

    private final String idamS2SBaseUri;
    private final String idamUserBaseUrl;

    @Autowired
    public AuthTokenProvider(@Value("${base-urls.idam-s2s}") String idamS2SBaseUri,
                             @Value("${base-urls.idam-user}") String idamUserBaseUri) {
        this.idamS2SBaseUri = idamS2SBaseUri;
        this.idamUserBaseUrl = idamUserBaseUri;
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
        return RestAssured
                .given().baseUri(idamS2SBaseUri)
                .param("microservice", "em_gw")
                .post("testing-support/lease")
                .andReturn().asString();
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
            return "AuthTokens{" +
                    "userToken='" + userToken + '\'' +
                    ", serviceToken='" + serviceToken + '\'' +
                    '}';
        }
    }
}
