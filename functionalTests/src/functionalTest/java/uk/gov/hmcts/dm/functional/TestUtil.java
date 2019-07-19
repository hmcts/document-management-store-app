package uk.gov.hmcts.dm.functional;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestUtil {

    private final String idamAuth;
    private final String s2sAuth;

    private final IdamHelper idamHelper;

    private final Map<String, String> idamAuthCache;

    public TestUtil() {
        this.idamHelper = new IdamHelper(Env.getIdamUrl(), Env.getOAuthClient(), Env.getOAuthSecret(), Env.getOAuthRedirect());
        idamAuthCache = new HashMap<>();

        RestAssured.useRelaxedHTTPSValidation();
        idamAuth = idamHelper.getIdamToken();

        final S2sHelper s2sHelper = new S2sHelper(Env.getS2sUrl(), Env.getS2sSecret(), Env.getS2sMicroservice());
        s2sAuth = s2sHelper.getS2sToken();
    }

    public RequestSpecification authRequest() {
        return RestAssured
            .given()
            .header("Authorization", idamAuth)
            .header("ServiceAuthorization", s2sAuth);
    }

    public RequestSpecification authRequest(String username, List<String> roles) {

        if (!idamAuthCache.containsKey(username)) {
            idamAuthCache.put(username, idamHelper.getIdamToken(username, roles));
        }

        return RestAssured
                .given()
                .header("Authorization", idamAuthCache.get(username))
                .header("ServiceAuthorization", s2sAuth);
    }
}
