package uk.gov.hmcts.dm.functional

import net.thucydides.core.annotations.Pending
import org.junit.Assert
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import uk.gov.hmcts.dm.functional.utilities.Classifications
import uk.gov.hmcts.reform.em.test.retry.RetryRule

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.is

@Ignore
class MetadataMigrationIT extends BaseIT {

    @Rule
    public RetryRule retryRule = new RetryRule(3);

    @Test
    @Pending
    // To remove once metadataMigrationEnabled toggle is enabled
    void "As a Shashank I want to process a CSV of metadata changes"() {
        if (metadataMigrationEnabled) {
            def document1Url = createDocumentAndGetUrlAs(CITIZEN)
            def document1Id = document1Url.split("/").last()
            def document2Url = createDocumentAndGetUrlAs(CITIZEN)
            def document2Id = document2Url.split("/").last()
            def body = "id,case_id,case_type_id,jurisdiction,document_id,document_url,case_created_date,case_last_modified_date,migrated\n" +
                "1,1,AAT,AUTOTEST1,${document1Id},http://dm-store:8080/documents/${document1Id},2020-02-28 14:51:10.592,2020-02-28 14:51:10.6,f\n" +
                "2,2,AAT,AUTOTEST1,${document2Id},http://dm-store:8080/documents/${document2Id},2020-03-02 11:10:56.615,2020-03-02 11:10:56.622,f"

            File file = File.createTempFile("migration", ".csv")
            file.write(body);

            postCsvFileAndTriggerSpringBatchJob(file)

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


    @Test
    @Pending
    // To remove once metadataMigrationEnabled toggle is enabled
    void "As a Shashank  I want metadata changes applied to only Matching records from CSV"() {
        if (metadataMigrationEnabled) {
            def document1Url = createDocumentAndGetUrlAs(CITIZEN)
            def document1Id = document1Url.split("/").last()

            def document2Url = createDocumentAndGetUrlAs(CITIZEN)
            def document2Id = document2Url.split("/").last()

            // Document3 created but not enriched via CSV
            def document3Url = createDocumentAndGetUrlAs(CITIZEN)
            def document3Id = document3Url.split("/").last()

            // only 2 records are in the CSV , 3rd one is not Present  and should not be Enriched by Metadata Migration.
            def body = "id,case_id,case_type_id,jurisdiction,document_id,document_url,case_created_date,case_last_modified_date,migrated\n" +
                "1,1,AAT,AUTOTEST1,${document1Id},http://dm-store:8080/documents/${document1Id},2020-02-28 14:51:10.592,2020-02-28 14:51:10.6,f\n" +
                "2,2,AAT,AUTOTEST1,${document2Id},http://dm-store:8080/documents/${document2Id},2020-03-02 11:10:56.615,2020-03-02 11:10:56.622,f"

            File file = File.createTempFile("migration", ".csv")
            file.write(body);

            postCsvFileAndTriggerSpringBatchJob(file)

            def metadata1 = fetchDocumentMetaDataAs CITIZEN, document1Url
            def caseId1 = metadata1.body().prettyPeek().jsonPath().get('metadata.case_id')
            def caseTypeId1 = metadata1.body().prettyPeek().jsonPath().get('metadata.case_type_id')
            def jurisdiction1 = metadata1.body().prettyPeek().jsonPath().get('metadata.jurisdiction')

            def metadata2 = fetchDocumentMetaDataAs CITIZEN, document2Url
            def caseId2 = metadata2.body().prettyPeek().jsonPath().get('metadata.case_id')
            def caseTypeId2 = metadata2.body().prettyPeek().jsonPath().get('metadata.case_type_id')
            def jurisdiction2 = metadata2.body().prettyPeek().jsonPath().get('metadata.jurisdiction')

            def metadata3 = fetchDocumentMetaDataAs CITIZEN, document3Url
            def caseId3 = metadata3.body().prettyPeek().jsonPath().get('metadata.case_id')
            def caseTypeId3 = metadata3.body().prettyPeek().jsonPath().get('metadata.case_type_id')
            def jurisdiction3 = metadata3.body().prettyPeek().jsonPath().get('metadata.jurisdiction')

            Assert.assertTrue caseId1 == "1"
            Assert.assertTrue caseTypeId1 == "AAT"
            Assert.assertTrue jurisdiction1 == "AUTOTEST1"

            Assert.assertTrue caseId2 == "2"
            Assert.assertTrue caseTypeId2 == "AAT"
            Assert.assertTrue jurisdiction2 == "AUTOTEST1"

            // Document3 must not be enriched with metadata as it was not present in the CSV File.
            Assert.assertNull(caseId3)
            Assert.assertNull(caseTypeId3)
            Assert.assertNull(jurisdiction3)
        }
    }

    @Test
    @Pending
    // To remove once metadataMigrationEnabled toggle is enabled
    void "As a As authenticated user  I want to process a CSV file which has missing metadata for one Case"() {
        if (metadataMigrationEnabled) {
            def document1Url = createDocumentAndGetUrlAs(CITIZEN)
            def document1Id = document1Url.split("/").last()

            def document2Url = createDocumentAndGetUrlAs(CITIZEN)
            def document2Id = document2Url.split("/").last()

            // missing metadata in the CSV file
            def body = "id,case_id,case_type_id,jurisdiction,document_id,document_url,case_created_date,case_last_modified_date,migrated\n" +
                ",,,,${document1Id},http://dm-store:8080/documents/${document1Id},2020-02-28 14:51:10.592,2020-02-28 14:51:10.6,f"

            File file = File.createTempFile("migration", ".csv")
            file.write(body);

            postCsvFileAndTriggerSpringBatchJob(file)

            def metadata1 = fetchDocumentMetaDataAs CITIZEN, document1Url
            def caseId1 = metadata1.body().prettyPeek().jsonPath().get('metadata.case_id')
            def caseTypeId1 = metadata1.body().prettyPeek().jsonPath().get('metadata.case_type_id')
            def jurisdiction1 = metadata1.body().prettyPeek().jsonPath().get('metadata.jurisdiction')

            assertThat(caseId1, is(""))
            assertThat(caseTypeId1, is(""))
            assertThat(jurisdiction1, is(""))
        }
    }

    @Test
    @Pending
    // To remove once metadataMigrationEnabled toggle is enabled
    void "As a As authenticated user I  want to process a CSV file which has missing metadata for one Case and valid metadata for Another"() {
        if (metadataMigrationEnabled) {
            def document1Url = createDocumentAndGetUrlAs(CITIZEN)
            def document1Id = document1Url.split("/").last()

            def document2Url = createDocumentAndGetUrlAs(CITIZEN)
            def document2Id = document2Url.split("/").last()

            // missing metadata in the CSV file
            def body = "id,case_id,case_type_id,jurisdiction,document_id,document_url,case_created_date,case_last_modified_date,migrated\n" +
                "1,,,,${document1Id},http://dm-store:8080/documents/${document1Id},2020-02-28 14:51:10.592,2020-02-28 14:51:10.6,f\n" +
                "2,2,AAT,AUTOTEST2,${document2Id},http://dm-store:8080/documents/${document2Id},2020-03-02 11:10:56.615,2020-03-02 11:10:56.622,f"

            File file = File.createTempFile("migration", ".csv")
            file.write(body);

            postCsvFileAndTriggerSpringBatchJob(file)

            def metadata1 = fetchDocumentMetaDataAs CITIZEN, document1Url
            def caseId1 = metadata1.body().prettyPeek().jsonPath().get('metadata.case_id')
            def caseTypeId1 = metadata1.body().prettyPeek().jsonPath().get('metadata.case_type_id')
            def jurisdiction1 = metadata1.body().prettyPeek().jsonPath().get('metadata.jurisdiction')

            def metadata2 = fetchDocumentMetaDataAs CITIZEN, document2Url
            def caseId2 = metadata2.body().prettyPeek().jsonPath().get('metadata.case_id')
            def caseTypeId2 = metadata2.body().prettyPeek().jsonPath().get('metadata.case_type_id')
            def jurisdiction2 = metadata2.body().prettyPeek().jsonPath().get('metadata.jurisdiction')

            assertThat(caseId1, is(""))
            assertThat(caseTypeId1, is(""))
            assertThat(jurisdiction1, is(""))

            assertThat(caseId2, is("2"))
            assertThat(caseTypeId2, is("AAT"))
            assertThat(jurisdiction2, is("AUTOTEST2"))
        }
    }


    @Test
    @Pending
    // To remove once metadataMigrationEnabled toggle is enabled
    void "As a As authenticated user I want to process a CSV file which has random documentId generated and metadata should not be updated"() {
        if (metadataMigrationEnabled) {
            def document1Url = createDocumentAndGetUrlAs(CITIZEN)
            def document1Id = document1Url.split("/").last()

            def document2Url = createDocumentAndGetUrlAs(CITIZEN)
            def document2Id = document2Url.split("/").last()

            def randomDocumentId = new Random().nextInt(5);

            // random DocumentId in CSV File
            def body = "1,1,AAT,AUTOTEST1,randomDocumentId,http://dm-store:8080/documents/randomDocumentId,2020-02-28 14:51:10.592,2020-02-28 14:51:10.6,f"

            File file = File.createTempFile("migration", ".csv")
            file.write(body);

            postCsvFileAndTriggerSpringBatchJob(file)

            def metadata1 = fetchDocumentMetaDataAs CITIZEN, document1Url
            def caseId1 = metadata1.body().prettyPeek().jsonPath().get('metadata.case_id')
            def caseTypeId1 = metadata1.body().prettyPeek().jsonPath().get('metadata.case_type_id')
            def jurisdiction1 = metadata1.body().prettyPeek().jsonPath().get('metadata.jurisdiction')

            Assert.assertNull(caseId1)
            Assert.assertNull(caseTypeId1)
            Assert.assertNull(jurisdiction1)
        }
    }


    def postCsvFileAndTriggerSpringBatchJob(file) {
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
    }
}
