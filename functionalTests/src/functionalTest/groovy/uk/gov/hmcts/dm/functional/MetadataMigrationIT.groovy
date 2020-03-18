package uk.gov.hmcts.dm.functional

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.junit4.SpringRunner
import uk.gov.hmcts.dm.functional.utilities.Classifications

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

        // let the job run
        sleep(15000)

        def metadata1 = fetchDocumentMetaDataAs CITIZEN, document1Url
        def caseId1 = metadata1.body().prettyPeek().jsonPath().get('metadata.case_id')
        def caseTypeId1 = metadata1.body().prettyPeek().jsonPath().get('metadata.case_type_id')
        def jurisdiction1 = metadata1.body().prettyPeek().jsonPath().get('metadata.jurisdiction')

        def metadata2 = fetchDocumentMetaDataAs CITIZEN, document2Url
        def caseId2 = metadata2.body().prettyPeek().jsonPath().get('metadata.case_id')
        def caseTypeId2 = metadata2.body().prettyPeek().jsonPath().get('metadata.case_type_id')
        def jurisdiction2 = metadata2.body().prettyPeek().jsonPath().get('metadata.jurisdiction')

        Assert.assertTrue caseId1 == "1"
        Assert.assertTrue caseTypeId1 == "AAT"
        Assert.assertTrue jurisdiction1 == "AUTOTEST1"
        Assert.assertTrue caseId2 == "2"
        Assert.assertTrue caseTypeId2 == "AAT"
        Assert.assertTrue jurisdiction2 == "AUTOTEST1"
    }


}
