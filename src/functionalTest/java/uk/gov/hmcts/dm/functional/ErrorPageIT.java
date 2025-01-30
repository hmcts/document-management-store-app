package uk.gov.hmcts.dm.functional;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.util.MimeTypeUtils.ALL_VALUE;
import static org.springframework.util.MimeTypeUtils.APPLICATION_XML_VALUE;

public class ErrorPageIT extends BaseIT {

    private static final String DOCUMENTS_CONST = "documents";
    private static final String FILES_CONST = "files";
    private static final String ROLES_CONST = "roles";
    private static final String CITIZEN_CONST = "citizen";
    private static final String CLASSIFICATION_CONST = "classification";
    private static final String ERROR_CONST = "error";
    private static final String DOCTYPE_HTML_CONST = "<!DOCTYPE html>";
    private static final String MIME_TYPE_GOV_APP =
        "application/vnd.uk.gov.hmcts.dm.document-collection.v1+hal+json,application/json;charset=UTF-8";
    private static final String ERROR_MESSAGE = "Your upload contains a disallowed file type";
    private static final String DOCUMENTS_PATH = "/documents";

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
            .accept(MIME_TYPE_GOV_APP)
            .expect()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .when()
            .get("documents/XXX");
    }

    @Test
    public void ep3AsAnAuthenticatedUserTryingToAccessDocumentDirReceiveJsonErrorPageWith405() {

        givenRequest(getCitizen())
            .accept(MIME_TYPE_GOV_APP)
            .expect()
            .statusCode(405)
            .contentType(ContentType.JSON)
            .when()
            .get(DOCUMENTS_CONST);
    }

    @Test
    public void ep4AsAnAuthenticatedUserTryingToPostNoDocumentReceiveJsonErrorPageWith415() {

        givenRequest(getCitizen())
            .accept(MIME_TYPE_GOV_APP)
            .expect()
            .statusCode(415)
            .contentType(ContentType.JSON)
            .when()
            .post(DOCUMENTS_CONST);
    }

    @Test
    @Disabled("exe seems to be blocked somewhere causing these tests to fail in CI")
    public void ep5AsAnAuthenticatedUserTryingToPostBadAttachmentReceiveJsonErrorPageWith415() {

        givenRequest(getCitizen())
            .accept(MIME_TYPE_GOV_APP)
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
            .accept(MIME_TYPE_GOV_APP)
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
            .body(not(containsString(DOCTYPE_HTML_CONST)))
            .when()
            .get(documentUrl);
    }

    @Test
    // ep9AsAnUnauthenticatedApiUserTryingToAccessADocumentWithDocumentAcceptHeaderReceiveJsonError
    public void ep8AsAnUnauthenticatedApiUserTryingToAccessADocumentWithNoAcceptHeaderReceiveJsonError() {

        String documentUrl = createDocumentAndGetUrlAs(getCitizen());

        givenUnauthenticatedRequest()
            .expect()
            .statusCode(403)
            .contentType(ContentType.JSON)
            .body(not(containsString(DOCTYPE_HTML_CONST)))
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
            .body(not(containsString(DOCTYPE_HTML_CONST)))
            .when()
            .post(DOCUMENTS_CONST);
    }

    @Test
    public void ep12AsAnAuthenticatedUserWhenIPostASvgDocumentIShouldGetJsonResponse() {

        givenRequest(getCitizen())
            .multiPart(FILES_CONST, file(getAttachment10()), V1MimeTypes.IMAGE_SVG_VALUE)
            .multiPart(CLASSIFICATION_CONST, String.valueOf(Classifications.PUBLIC))
            .multiPart(ROLES_CONST, CITIZEN_CONST)
            .expect().log().all()
            .statusCode(422)
            .body(ERROR_CONST, equalTo(ERROR_MESSAGE))
            .contentType(ContentType.JSON)
            .when()
            .post(DOCUMENTS_PATH);
    }

    @Test
    public void ep13AsAnAuthenticatedUserWhenIPostAXmlDocumentIShouldGetJsonResponse() {

        givenRequest(getCitizen())
            .multiPart(FILES_CONST, file(getAttachment18()), APPLICATION_XML_VALUE)
            .multiPart(CLASSIFICATION_CONST, String.valueOf(Classifications.PUBLIC))
            .multiPart(ROLES_CONST, CITIZEN_CONST)
            .expect().log().all()
            .statusCode(422)
            .body(ERROR_CONST, equalTo(ERROR_MESSAGE))
            .contentType(ContentType.JSON)
            .when()
            .post(DOCUMENTS_PATH);
    }

    @Test
    @Disabled("exe seems to be blocked somewhere causing these tests to fail in CI")
    public void ep14AsAnAuthenticatedUserWhenIPostAExeDocumentIShouldGetJsonResponse() {

        givenRequest(getCitizen())
            .multiPart(FILES_CONST, file(getBadAttachment1()), ALL_VALUE)
            .multiPart(CLASSIFICATION_CONST, String.valueOf(Classifications.PUBLIC))
            .multiPart(ROLES_CONST, CITIZEN_CONST)
            .expect().log().all()
            .statusCode(422)
            .body(ERROR_CONST, equalTo(ERROR_MESSAGE))
            .contentType(ContentType.JSON)
            .when()
            .post(DOCUMENTS_PATH);
    }
}
