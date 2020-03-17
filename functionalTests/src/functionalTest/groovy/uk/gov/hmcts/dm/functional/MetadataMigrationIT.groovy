package uk.gov.hmcts.dm.functional

import io.restassured.response.Response
import org.apache.http.entity.ContentType
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import uk.gov.hmcts.dm.functional.utilities.Classifications
import uk.gov.hmcts.dm.functional.utilities.V1MediaTypes
import uk.gov.hmcts.dm.functional.utilities.V1MimeTypes

import java.time.Duration
import java.time.LocalDateTime

import static org.hamcrest.Matchers.*
import static org.junit.Assume.assumeTrue

@RunWith(SpringRunner.class)
class MetadataMigrationIT extends BaseIT {

    @Test
    void "As a Shashank I want to process a CSV of metadata changes"() {
        def document1Url = createDocumentAndGetUrlAs(CITIZEN)
        def document1Id = document1Url.split("/").last()
        def document2Url = createDocumentAndGetUrlAs(CITIZEN)
        def document2Id = document2Url.split("/").last()
        def body = "id,case_id,case_type_id,jurisdiction,document_id,document_url,case_created_date,case_last_modified_date,migrated\n" +
            "1,1,AAT,AUTOTEST1,${document1Id},http://dm-store:8080/documents/${document1Id},2020-02-28 14:51:10.592,2020-02-28 14:51:10.6,f\n" +
            "2,2,AAT,AUTOTEST1,${document2Id},http://dm-store:8080/documents/${document2Id},2020-03-02 11:10:56.615,2020-03-02 11:10:56.622,f"

        File file = File.createTempFile("migration", ".csv")
        file.write(body);

        givenRequest(CITIZEN)
            .multiPart("files", file, "text/csv")
            .multiPart("classification", Classifications.PUBLIC as String)
            .multiPart("roles", "citizen")
            .multiPart("roles", "caseworker")
            .expect()
            .statusCode(200)
            .when()
            .post("/testing/metadata-migration-csv")

    }


}
