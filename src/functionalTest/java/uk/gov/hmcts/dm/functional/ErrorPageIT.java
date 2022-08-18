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
    public void EP1_As_an_unauthenticated_web_user_trying_to_access_a_document__receive_JSON_error_page_with_403() {

        String documentUrl = createDocumentAndGetUrlAs(getCITIZEN());

        givenUnauthenticatedRequest()
            .expect()
            .statusCode(403)
            .contentType(ContentType.JSON)
            .when()
            .get(documentUrl);
    }

    @Test
    public void EP2_As_an_authenticated_user_trying_to_access_an_unknown_document__receive_JSON_error_page_with_404() {

        givenRequest(getCITIZEN())
            .accept("application/vnd.uk.gov.hmcts.dm.document-collection.v1+hal+json,application/json;charset=UTF-8")
            .expect()
            .statusCode(404)
            .contentType(ContentType.JSON)
            .when()
            .get("documents/XXX");
    }

    @Test
    public void EP3_As_an_authenticated_user_trying_to_access_document_dir__receive_JSON_error_page_with_405() {

        givenRequest(getCITIZEN())
            .accept("application/vnd.uk.gov.hmcts.dm.document-collection.v1+hal+json,application/json;charset=UTF-8")
            .expect()
            .statusCode(405)
            .contentType(ContentType.JSON)
            .when()
            .get("documents/");
    }

    @Test
    public void EP4_As_an_authenticated_user_trying_to_post_no_document__receive_JSON_error_page_with_415() {

        givenRequest(getCITIZEN())
            .accept("application/vnd.uk.gov.hmcts.dm.document-collection.v1+hal+json,application/json;charset=UTF-8")
            .expect()
            .statusCode(415)
            .contentType(ContentType.JSON)
            .when()
            .post("documents/");
    }

    @Test
    @Ignore("exe seems to be blocked somewhere causing these tests to fail in CI")
    public void EP5_As_an_authenticated_user_trying_to_post_bad_attachment__receive_JSON_error_page_with_415() {

        givenRequest(getCITIZEN())
            .accept("application/vnd.uk.gov.hmcts.dm.document-collection.v1+hal+json,application/json;charset=UTF-8")
            .multiPart("file", file(getBAD_ATTACHMENT_1()), MediaType.ALL_VALUE)
            .expect()
            .statusCode(422)
            .contentType(ContentType.JSON)
            .when()
            .post("documents/");
    }

    @Test
    public void EP6_As_an_authenticated_user_but_not_the_owner_of_the_file__post_the_newer_version_of_the_file__receive_JSON_error_page_with_403() {

        String url = createDocumentAndGetUrlAs(getCITIZEN());

        givenRequest(getCITIZEN_2())
            .accept("application/vnd.uk.gov.hmcts.dm.document-collection.v1+hal+json,application/json;charset=UTF-8")
            .multiPart("file", file(getATTACHMENT_9_JPG()), MediaType.IMAGE_JPEG_VALUE)
            .expect()
            .statusCode(403)
            .contentType(ContentType.JSON)
            .when()
            .post(url);
    }

    @Test
    public void EP7_As_an_unauthenticated_api_user_trying_to_access_a_document_with_accept_JSON__receive_JSON_error() {

        String documentUrl = createDocumentAndGetUrlAs(getCITIZEN());

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
    public void EP8_As_an_unauthenticated_api_user_trying_to_access_a_document_with_no_accept_header__receive_JSON_error() {

        String documentUrl = createDocumentAndGetUrlAs(getCITIZEN());

        givenUnauthenticatedRequest()
            .expect()
            .statusCode(403)
            .contentType(ContentType.JSON)
            .body(not(containsString("<!DOCTYPE html>")))
            .when()
            .get(documentUrl);
    }

    @Test
    public void EP9_As_an_unauthenticated_api_user_trying_to_access_a_document_with_document_accept_header__receive_JSON_error() {

        String documentUrl = createDocumentAndGetUrlAs(getCITIZEN());

        givenUnauthenticatedRequest()
            .expect()
            .statusCode(403)
            .contentType(ContentType.JSON)
            .body(not(containsString("<!DOCTYPE html>")))
            .when()
            .get(documentUrl);
    }

    @Test
    public void EP10_As_an_authenticated_user_but_not_the_owner_of_the_file__post_the_newer_version_of_the_file__receive_JSON_error_page_with_403() {

        String url = createDocumentAndGetUrlAs(getCITIZEN());

        givenRequest(getCITIZEN())
            .accept(ContentType.JSON)
            .multiPart("file", file(getATTACHMENT_18()), MediaType.APPLICATION_XML_VALUE)
            .expect()
            .statusCode(422)
            .contentType(ContentType.JSON)
            .when()
            .post(url);
    }

    @Test
    public void EP11_As_an_authenticated_web_user_trying_to_post_no_document__receive_JSON_error_page_with_422() {

        givenRequest(getCITIZEN())
            .accept(ContentType.XML)
            .multiPart("file", file(getATTACHMENT_18()), MediaType.APPLICATION_XML_VALUE)
            .expect()
            .statusCode(422)
            .body(not(containsString("<!DOCTYPE html>")))
            .when()
            .post("documents/");
    }

    @Test
    public void EP12_As_an_authenticated_user__when_I_post_a_SVG_document_I_should_get_JSON_response() {

        givenRequest(getCITIZEN())
            .multiPart("files", file(getATTACHMENT_10()), V1MimeTypes.IMAGE_SVG_VALUE)
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
    public void EP13_As_an_authenticated_user__when_I_post_a_XML_document_I_should_get_JSON_response() {

        givenRequest(getCITIZEN())
            .multiPart("files", file(getATTACHMENT_18()), V1MimeTypes.APPLICATION_XML_VALUE)
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
    public void EP14_As_an_authenticated_user__when_I_post_a_EXE_document_I_should_get_JSON_response() {

        givenRequest(getCITIZEN())
            .multiPart("files", file(getBAD_ATTACHMENT_1()), V1MimeTypes.ALL_VALUE)
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
