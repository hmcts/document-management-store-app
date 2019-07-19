package uk.gov.hmcts.dm.functional;

import org.apache.commons.lang3.Validate;

import java.util.Properties;

public final class Env {
    static Properties defaults = new Properties();

    static {
        defaults.setProperty("PROXY", "false");
        defaults.setProperty("TEST_URL", "http://localhost:8080");
        defaults.setProperty("IDAM_API_BASE_URI", "http://localhost:4501");
        defaults.setProperty("OAUTH_CLIENT", "webshow");
        defaults.setProperty("IDAM_WEBSHOW_WHITELIST", "http://localhost:8080/oauth2redirect");
        defaults.setProperty("FUNCTIONAL_TEST_CLIENT_OAUTH_SECRET", "AAAAAAAAAAAAAAAA");
        defaults.setProperty("S2S_BASE_URI", "http://localhost:4502");
        defaults.setProperty("FUNCTIONAL_TEST_CLIENT_S2S_TOKEN", "AAAAAAAAAAAAAAAA");
        defaults.setProperty("S2S_SERVICE_NAME", "em_gw");
    }

    protected Env() {
        // prevents calls from subclass
        throw new UnsupportedOperationException();
    }

    public static String getUseProxy() {
        return require("PROXY");
    }

    public static String getTestUrl() {
        return require("TEST_URL");
    }

    public static String require(String name) {
        return Validate.notNull(System.getenv(name) == null ? defaults.getProperty(name) : System.getenv(name), "Environment variable `%s` is required", name);
    }

    public static String getIdamUrl() {
        return require("IDAM_API_BASE_URI");
    }

    public static String getOAuthClient() {
        return require("OAUTH_CLIENT");
    }

    public static String getOAuthRedirect() {
        return require("IDAM_WEBSHOW_WHITELIST");
    }

    public static String getOAuthSecret() {
        return require("FUNCTIONAL_TEST_CLIENT_OAUTH_SECRET");
    }

    public static String getS2sUrl() {
        return require("S2S_BASE_URI");
    }

    public static String getS2sSecret() {
        return require("FUNCTIONAL_TEST_CLIENT_S2S_TOKEN");
    }

    public static String getS2sMicroservice() {
        return require("S2S_SERVICE_NAME");
    }
}
