package uk.gov.hmcts.dm.functional;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import io.restassured.RestAssured;
import org.json.JSONObject;

public class S2sHelper {

    private final String s2sUrl;
    private final String totpSecret;
    private final String microservice;

    public S2sHelper(String s2sUrl, String totpSecret, String microservice) {
        this.s2sUrl = s2sUrl;
        this.totpSecret = totpSecret;
        this.microservice = microservice;
    }

    public String getS2sToken() {
        String otp = String.valueOf(new GoogleAuthenticator().getTotpPassword(totpSecret));

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("microservice", microservice);
        jsonObject.put("oneTimePassword", otp);

        return "Bearer " + RestAssured
            .given()
            .header("Content-Type", "application/json")
            .body(jsonObject.toString())
            .post(s2sUrl + "/lease")
            .getBody()
            .asString();
    }
}
