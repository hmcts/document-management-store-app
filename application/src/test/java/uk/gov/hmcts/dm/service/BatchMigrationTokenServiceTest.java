package uk.gov.hmcts.dm.service;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.dm.exception.ValidationErrorException;

import javax.crypto.BadPaddingException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static uk.gov.hmcts.dm.service.RsaPublicKeyReaderTest.PUBLIC_KEY_STRING;



public class BatchMigrationTokenServiceTest {

    private BatchMigrationTokenService underTest;

    private static final String AUTH_TOKEN = "2ztGcF6WhLZrBFyeKcl/m5ToPgnQfodOH4xeqaCnCSS/CydCNANpjLdWlbEedDetcl4c1W"
        + "/XRBbc6t8ROKk3OmazcxjhmC5MZxC1t1Pbto/H4aqF59bvsy7lsBf/yXbmN/Fu1HLZqsMchEPbOZiIjkyEY"
        + "/J1Ot8CUFgWrf43bkDridevKrg65pRGv3cAMdSnnHYTZB+XvqMPmK9xwVD2wBjM+s3TwGO7fdqb+cWs"
        + "/ZyZrirU35/6LpIT1iPf7fH7lV0CZ/GXKQ95ssqHa7STtZRXy61vCwSpO4J5jLI7IOuKWSwnCcw8xkoQinz6gS"
        + "/rO8kbWwBF1cI+I1psu/f9+w==";

    @Before
    public void setUp() {
        underTest = new BatchMigrationTokenService(new RsaPublicKeyReader());
        underTest.setMigrationPublicKeyStringValue(PUBLIC_KEY_STRING);
    }

    @Test
    public void acceptBadAuthTokenInNoProductionMode() {
        final RsaPublicKeyReader rsaPublicKeyReader = mock(RsaPublicKeyReader.class);
        underTest = new BatchMigrationTokenService(rsaPublicKeyReader);
        underTest.setPublicKeyRequired(false);
        underTest.checkAuthToken("Some Token");

        verifyZeroInteractions(rsaPublicKeyReader);
    }

    @Test(expected = BadPaddingException.class)
    public void acceptNoBadAuthTokenInProductionMode() throws Throwable {
        try {
            underTest.setPublicKeyRequired(true);
            underTest.checkAuthToken("Some Token");
        } catch (ValidationErrorException e) {
            throw e.getCause();
        }
        fail("BadPaddingException is expected");
    }

    @Test
    public void acceptMatchedToken() {
        underTest.setPublicKeyRequired(true);
        underTest.setMigrateSecret("y2hahvdZ9evcTVq2");
        underTest.checkAuthToken(AUTH_TOKEN);
    }

    @Test(expected = ValidationErrorException.class)
    public void acceptNoMatchedToken() {
        underTest.setPublicKeyRequired(true);
        underTest.setMigrateSecret("secretNoMatched");
        try {
            underTest.checkAuthToken(AUTH_TOKEN);
        } catch (ValidationErrorException e) {
            assertThat(e.getMessage(), is("Incorrect secret"));
            throw e;
        }
    }

    @Test(expected = ValidationErrorException.class)
    public void nullAuthToken() {
        underTest.setPublicKeyRequired(true);
        underTest.setMigrateSecret("Expects a Token");
        try {
            underTest.checkAuthToken(null);
        } catch (ValidationErrorException e) {
            assertThat(e.getMessage(), is("An auth token is expected"));
            throw e;
        }
    }

    @Test
    public void acceptsNullAuthTokenIfNotRequired() {
        final RsaPublicKeyReader rsaPublicKeyReader = mock(RsaPublicKeyReader.class);
        underTest = new BatchMigrationTokenService(rsaPublicKeyReader);

        underTest.setPublicKeyRequired(false);
        underTest.setMigrateSecret("Not Required");

        underTest.checkAuthToken(null);
        verifyZeroInteractions(rsaPublicKeyReader);
    }
}
