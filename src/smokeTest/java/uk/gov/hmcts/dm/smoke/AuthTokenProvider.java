package uk.gov.hmcts.dm.smoke;

import io.restassured.RestAssured;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AuthTokenProvider {

    private final String idamS2SBaseUri;

    private final String token;

    @Autowired
    public AuthTokenProvider(@Value("${base-urls.idam-s2s}") String idamS2SBaseUri,
                             @Value("${login.token}")String token
    ) {
        this.idamS2SBaseUri = idamS2SBaseUri;

        this.token = token;
        System.out.println("IDAM S2S URL - " + idamS2SBaseUri);
        System.out.println("JWT token - " + token);
    }

    public AuthTokens getTokens() {
        return new AuthTokens(token, "");
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

        @Override
        public String toString() {
            return "AuthTokens{"
                + "userToken='" + userToken + '\''
                + ", serviceToken='" + serviceToken + '\''
                + '}';
        }
    }

    public String findServiceToken() {
        return RestAssured
            .given().baseUri(idamS2SBaseUri)
            .param("microservice", "em_gw")
            .post("testing-support/lease")
            .andReturn().asString();
    }





}
