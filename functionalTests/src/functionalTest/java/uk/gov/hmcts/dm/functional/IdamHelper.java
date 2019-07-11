package uk.gov.hmcts.dm.functional;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

public class IdamHelper {

    private static final String USERNAME = "testytesttest@test.net";
    private static final String PASSWORD = "4590fgvhbfgbDdffm3lk4j";

    private final String idamUrl;
    private final String client;
    private final String secret;
    private final String redirect;

    public IdamHelper(String idamUrl, String client, String secret, String redirect) {
        this.idamUrl = idamUrl;
        this.client = client;
        this.secret = secret;
        this.redirect = redirect;
    }

    public String getIdamToken() {
        createUser(USERNAME, null);

        String code = getCode(USERNAME);
        String token = getToken(code);

        return "Bearer " + token;
    }

    public String getIdamToken(String username, List<String> roles) {
        createUser(username, roles);

        String code = getCode(username);
        String token = getToken(code);

        return "Bearer " + token;
    }

    private void createUser(String username, List<String> roles) {
        deleteUser(username);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("email", username);
        jsonObject.put("password", PASSWORD);
        jsonObject.put("forename", "test");
        jsonObject.put("surname", "test");
        jsonObject.put("roles", roles == null || roles.size() == 0 ? null : new JSONArray(roles.stream().map( role -> {
            JSONObject roleObject = new JSONObject();
            roleObject.put("code", role);
            return roleObject;
        }).collect(Collectors.toList())));

        RestAssured
            .given().log().all()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(jsonObject.toString())
            .post(idamUrl + "/testing-support/accounts");
    }

    private void deleteUser(String username) {
        RestAssured
            .given().log().all()
            .delete(idamUrl + "/testing-support/accounts/"+username);
    }

    private String getCode(String username) {
        String credentials = username + ":" + PASSWORD;
        String authHeader = Base64.getEncoder().encodeToString(credentials.getBytes());

        JsonPath jsonPath = RestAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .header("Authorization", "Basic " + authHeader)
            .formParam("redirect_uri", redirect)
            .formParam("client_id", client)
            .formParam("response_type", "code")
            .post(idamUrl + "/oauth2/authorize")
            .jsonPath();

        return jsonPath.get("code");
    }

    private String getToken(String code) {
        return RestAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .formParam("code", code)
            .formParam("grant_type", "authorization_code")
            .formParam("redirect_uri", redirect)
            .formParam("client_id", client)
            .formParam("client_secret", secret)
            .post(idamUrl + "/oauth2/token")
            .jsonPath()
            .getString("access_token");
    }
}
