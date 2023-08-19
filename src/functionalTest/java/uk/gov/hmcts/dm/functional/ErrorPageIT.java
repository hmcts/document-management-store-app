package uk.gov.hmcts.dm.functional;

import io.restassured.http.ContentType;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.em.test.retry.RetryRule;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class ErrorPageIT extends BaseIT {

    @Rule
    public RetryRule retryRule = new RetryRule(3);

    @Test
    public void ep1AsAnUnauthenticatedWebUserTryingToAccessADocumentReceiveJsonErrorPageWith403() {

        String documentUrl = createDocumentAndGetUrlAs(getCitizen());

        givenUnauthenticatedRequest()
            .expect()
            .statusCode(403)
            .contentType(ContentType.JSON)
            .when()
            .get(documentUrl);
    }

    @Test
    public void ep2AsAnAuthenticatedUserTryingToAccessAnUnknownDocumentReceiveJsonErrorPageWith404() {

        givenRequest(getCitizen())
            .accept("application/vnd.uk.gov.hmcts.dm.document-collection.v1+hal+json,application/json;charset=UTF-8")
            .expect()
            .statusCode(404)
            .contentType(ContentType.JSON)
            .when()
            .get("documents/XXX");
    }

    @Test
    public void ep3AsAnAuthenticatedUserTryingToAccessDocumentDirReceiveJsonErrorPageWith405() {

        givenRequest(getCitizen())
            .accept("application/vnd.uk.gov.hmcts.dm.document-collection.v1+hal+json,application/json;charset=UTF-8")
            .expect()
            .statusCode(405)
            .contentType(ContentType.JSON)
            .when()
            .get("documents/");
    }

    @Test
    public void ep4AsAnAuthenticatedUserTryingToPostNoDocumentReceiveJsonErrorPageWith415() {

        givenRequest(getCitizen())
            .accept("application/vnd.uk.gov.hmcts.dm.document-collection.v1+hal+json,application/json;charset=UTF-8")
            .expect()
            .statusCode(415)
            .contentType(ContentType.JSON)
            .when()
            .post("documents/");
    }

    @Test
    @Ignore("exe seems to be blocked somewhere causing these tests to fail in CI")
    public void ep5AsAnAuthenticatedUserTryingToPostBadAttachmentReceiveJsonErrorPageWith415() {

        givenRequest(getCitizen())
            .accept("application/vnd.uk.gov.hmcts.dm.document-collection.v1+hal+json,application/json;charset=UTF-8")
            .multiPart("file", file(getBadAttachment1()), MediaType.ALL_VALUE)
            .expect()
            .statusCode(422)
            .contentType(ContentType.JSON)
            .when()
            .post("documents/");
    }

    @Test
    public void ep6AsAnAuthenticatedUserNotTheOwnerOfTheFilePostTheNewerVersionOfTheFileReceiveJsonErrorPageWith403() {

        String url = createDocumentAndGetUrlAs(getCitizen());

        givenRequest(getCitizen2())
            .accept("application/vnd.uk.gov.hmcts.dm.document-collection.v1+hal+json,application/json;charset=UTF-8")
            .multiPart("file", file(getAttachment9Jpg()), MediaType.IMAGE_JPEG_VALUE)
            .expect()
            .statusCode(403)
            .contentType(ContentType.JSON)
            .when()
            .post(url);
    }

    @Test
    public void ep7AsAnUnauthenticatedApiUserTryingToAccessADocumentWithAcceptJsonReceiveJsonError() {

        String documentUrl = createDocumentAndGetUrlAs(getCitizen());

        givenUnauthenticatedRequest()
            .accept(ContentType.JSON)
            .expect()
            .statusCode(403)
            .contentType(ContentType.JSON)
            .body(not(containsString("<!DOCTYPE html>")))
            .when()
            .get(documentUrl);
    }

    @Test
    public void ep8AsAnUnauthenticatedApiUserTryingToAccessADocumentWithNoAcceptHeaderReceiveJsonError() {

        String documentUrl = createDocumentAndGetUrlAs(getCitizen());

        givenUnauthenticatedRequest()
            .expect()
            .statusCode(403)
            .contentType(ContentType.JSON)
            .body(not(containsString("<!DOCTYPE html>")))
            .when()
            .get(documentUrl);
    }

    @Test
    public void ep9AsAnUnauthenticatedApiUserTryingToAccessADocumentWithDocumentAcceptHeaderReceiveJsonError() {

        String documentUrl = createDocumentAndGetUrlAs(getCitizen());

        givenUnauthenticatedRequest()
            .expect()
            .statusCode(403)
            .contentType(ContentType.JSON)
            .body(not(containsString("<!DOCTYPE html>")))
            .when()
            .get(documentUrl);
    }

    @Test
    public void ep10AsAnAuthenticatedUserNotTheOwnerOfTheFilePostTheNewerVersionOfTheFileReceiveJsonErrorPageWith403() {

        String url = createDocumentAndGetUrlAs(getCitizen());

        givenRequest(getCitizen())
            .accept(ContentType.JSON)
            .multiPart("file", file(getAttachment18()), MediaType.APPLICATION_XML_VALUE)
            .expect()
            .statusCode(422)
            .contentType(ContentType.JSON)
            .when()
            .post(url);
    }

    @Test
    public void ep11AsAnAuthenticatedWebUserTryingToPostNoDocumentReceiveJsonErrorPageWith422() {

        givenRequest(getCitizen())
            .accept(ContentType.XML)
            .multiPart("file", file(getAttachment18()), MediaType.APPLICATION_XML_VALUE)
            .expect()
            .statusCode(422)
            .body(not(containsString("<!DOCTYPE html>")))
            .when()
            .post("documents/");
    }

    @Test
    public void ep12AsAnAuthenticatedUserWhenIPostASvgDocumentIShouldGetJsonResponse() {

        givenRequest(getCitizen())
            .multiPart("files", file(getAttachment10()), V1MimeTypes.IMAGE_SVG_VALUE)
            .multiPart("classification", String.valueOf(Classifications.PUBLIC))
            .multiPart("roles", "citizen")
            .expect().log().all()
            .statusCode(422)
            .body("error", equalTo("Your upload contains a disallowed file type"))
            .contentType(ContentType.JSON)
            .when()
            .post("/documents");
    }

    @Test
    public void ep13AsAnAuthenticatedUserWhenIPostAXmlDocumentIShouldGetJsonResponse() {

        givenRequest(getCitizen())
            .multiPart("files", file(getAttachment18()), V1MimeTypes.APPLICATION_XML_VALUE)
            .multiPart("classification", String.valueOf(Classifications.PUBLIC))
            .multiPart("roles", "citizen")
            .expect().log().all()
            .statusCode(422)
            .body("error", equalTo("Your upload contains a disallowed file type"))
            .contentType(ContentType.JSON)
            .when()
            .post("/documents");
    }

    @Test
    @Ignore("exe seems to be blocked somewhere causing these tests to fail in CI")
    public void ep14AsAnAuthenticatedUserWhenIPostAExeDocumentIShouldGetJsonResponse() {

        givenRequest(getCitizen())
            .multiPart("files", file(getBadAttachment1()), V1MimeTypes.ALL_VALUE)
            .multiPart("classification", String.valueOf(Classifications.PUBLIC))
            .multiPart("roles", "citizen")
            .expect().log().all()
            .statusCode(422)
            .body("error", equalTo("Your upload contains a disallowed file type"))
            .contentType(ContentType.JSON)
            .when()
            .post("/documents");
    }
}
