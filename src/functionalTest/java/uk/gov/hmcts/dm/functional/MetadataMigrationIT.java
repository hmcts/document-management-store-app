package uk.gov.hmcts.dm.functional;

import io.restassured.response.Response;
import net.serenitybdd.annotations.Pending;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SuppressWarnings("java:S5960") // Suppress SonarQube warning for assertions
public class MetadataMigrationIT extends BaseIT {

    private static final String MIGRATION_CONST = "migration";
    private static final String METADATA_CASE_ID_CONST = "metadata.case_id";
    private static final String METADATA_JURISDICTION_CONST = "metadata.jurisdiction";
    private static final String METADATA_CASE_TYPE_ID_CONST = "metadata.case_type_id";
    private static final String AUTOTEST1_CONST = "AUTOTEST1";
    private static final String URL_WITH_COMMA = ",http://dm-store:8080/documents/";
    private static final String DATE_TIME_WITH_TEXT = ",2020-03-02 11:10:56.615,2020-03-02 11:10:56.622,f";

    @Test
    @Pending
    public void asACitizenIWantToProcessACsvOfMetadataChanges() throws IOException, InterruptedException {
        if (getMetadataMigrationEnabled()) {
            String document1Url = createDocumentAndGetUrlAs(getCitizen());
            String[] split = document1Url
                .split("/");
            String document1Id = split[split.length - 1];
            String document2Url = createDocumentAndGetUrlAs(getCitizen());
            split = document2Url
                .split("/");
            String document2Id = split[split.length - 1];
            String body = "id,case_id,case_type_id,jurisdiction,document_id,document_url,"
                + "case_created_date,case_last_modified_date,migrated\n"
                + "1,1,AAT,AUTOTEST1," + document1Id + URL_WITH_COMMA + document1Id
                + ",2020-02-28 14:51:10.592,2020-02-28 14:51:10.6,f\n" + "2,2,AAT,AUTOTEST1," + document2Id
                + URL_WITH_COMMA + document2Id + DATE_TIME_WITH_TEXT;

            File file = File.createTempFile(MIGRATION_CONST, ".csv");
            try (FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.write(body);
            }

            postCsvFileAndTriggerSpringBatchJob(file);

            Response metadata1 = fetchDocumentMetaDataAs(getCitizen(), document1Url);

            Object caseId1 = metadata1.body().prettyPeek().jsonPath().get(METADATA_CASE_ID_CONST);
            assertEquals("1", caseId1);
            Object caseTypeId1 = metadata1.body().prettyPeek().jsonPath().get(METADATA_CASE_TYPE_ID_CONST);
            assertEquals("AAT", caseTypeId1);
            Object jurisdiction1 = metadata1.body().prettyPeek().jsonPath().get(METADATA_JURISDICTION_CONST);
            assertEquals(AUTOTEST1_CONST, jurisdiction1);

            Response metadata2 = fetchDocumentMetaDataAs(getCitizen(), document2Url);
            Object caseId2 = metadata2.body().prettyPeek().jsonPath().get(METADATA_CASE_ID_CONST);
            assertEquals("2", caseId2);
            Object caseTypeId2 = metadata2.body().prettyPeek().jsonPath().get(METADATA_CASE_TYPE_ID_CONST);
            assertEquals("AAT", caseTypeId2);
            Object jurisdiction2 = metadata2.body().prettyPeek().jsonPath().get(METADATA_JURISDICTION_CONST);
            assertEquals(AUTOTEST1_CONST, jurisdiction2);
        }

    }

    @Test
    @Pending
    public void asACitizenIWantMetadataChangesAppliedToOnlyMatchingRecordsFromCsv()
        throws IOException, InterruptedException {
        if (getMetadataMigrationEnabled()) {
            String document1Url = createDocumentAndGetUrlAs(getCitizen());
            String[] split = document1Url
                .split("/");
            String document1Id = split[split.length - 1];
            String document2Url = createDocumentAndGetUrlAs(getCitizen());
            split = document2Url
                .split("/");
            String document2Id = split[split.length - 1];

            // only 2 records are in the CSV , 3rd one is not Present  and should not be Enriched by Metadata Migration.
            String body = "id,case_id,case_type_id,jurisdiction,document_id,document_url,"
                + "case_created_date,case_last_modified_date,migrated\n"
                + "1,1,AAT,AUTOTEST1," + document1Id + URL_WITH_COMMA + document1Id
                + ",2020-02-28 14:51:10.592,2020-02-28 14:51:10.6,f\n" + "2,2,AAT,AUTOTEST1," + document2Id
                + URL_WITH_COMMA + document2Id + DATE_TIME_WITH_TEXT;

            File file = File.createTempFile(MIGRATION_CONST, ".csv");
            try (FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.write(body);
            }

            postCsvFileAndTriggerSpringBatchJob(file);

            Response metadata1 = fetchDocumentMetaDataAs(getCitizen(), document1Url);
            Object caseId1 = metadata1.body().prettyPeek().jsonPath().get(METADATA_CASE_ID_CONST);
            Object caseTypeId1 = metadata1.body().prettyPeek().jsonPath().get(METADATA_CASE_TYPE_ID_CONST);
            Object jurisdiction1 = metadata1.body().prettyPeek().jsonPath().get(METADATA_JURISDICTION_CONST);

            assertEquals("1", caseId1);
            assertEquals("AAT", caseTypeId1);
            assertEquals(AUTOTEST1_CONST, jurisdiction1);

            Response metadata2 = fetchDocumentMetaDataAs(getCitizen(), document2Url);
            Object caseTypeId2 = metadata2.body().prettyPeek().jsonPath().get(METADATA_CASE_TYPE_ID_CONST);
            Object jurisdiction2 = metadata2.body().prettyPeek().jsonPath().get(METADATA_JURISDICTION_CONST);
            Object caseId2 = metadata2.body().prettyPeek().jsonPath().get(METADATA_CASE_ID_CONST);
            assertEquals("2", caseId2);
            assertEquals("AAT", caseTypeId2);
            assertEquals(AUTOTEST1_CONST, jurisdiction2);

            // Document3 created but not enriched via CSV
            String document3Url = createDocumentAndGetUrlAs(getCitizen());
            Response metadata3 = fetchDocumentMetaDataAs(getCitizen(), document3Url);
            Object caseId3 = metadata3.body().prettyPeek().jsonPath().get(METADATA_CASE_ID_CONST);
            Object caseTypeId3 = metadata3.body().prettyPeek().jsonPath().get(METADATA_CASE_TYPE_ID_CONST);
            Object jurisdiction3 = metadata3.body().prettyPeek().jsonPath().get(METADATA_JURISDICTION_CONST);

            // Document3 must not be enriched with metadata as it was not present in the CSV File.
            assertNull(caseId3);
            assertNull(caseTypeId3);
            assertNull(jurisdiction3);
        }

    }

    @Test
    @Pending
    public void asAnAuthenticatedUserIWantToProcessACsvFileWhichHasMissingMetadataForOneCase()
        throws IOException, InterruptedException {
        if (getMetadataMigrationEnabled()) {
            String document1Url = createDocumentAndGetUrlAs(getCitizen());
            String[] split = document1Url
                .split("/");
            String document1Id = split[split.length - 1];

            // missing metadata in the CSV file
            String body = "id,case_id,case_type_id,jurisdiction,document_id,document_url,"
                + "case_created_date,case_last_modified_date,migrated\n"
                + ",,,," + document1Id + URL_WITH_COMMA
                + document1Id + ",2020-02-28 14:51:10.592,2020-02-28 14:51:10.6,f";

            File file = File.createTempFile(MIGRATION_CONST, ".csv");
            try (FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.write(body);
            }

            postCsvFileAndTriggerSpringBatchJob(file);

            Response metadata1 = fetchDocumentMetaDataAs(getCitizen(), document1Url);
            Object caseId1 = metadata1.body().prettyPeek().jsonPath().get(METADATA_CASE_ID_CONST);
            Object caseTypeId1 = metadata1.body().prettyPeek().jsonPath().get(METADATA_CASE_TYPE_ID_CONST);
            Object jurisdiction1 = metadata1.body().prettyPeek().jsonPath().get(METADATA_JURISDICTION_CONST);

            MatcherAssert.assertThat(caseId1, Matchers.is(""));
            MatcherAssert.assertThat(caseTypeId1, Matchers.is(""));
            MatcherAssert.assertThat(jurisdiction1, Matchers.is(""));
        }

    }

    @Test
    @Pending
    public void asAnAuthenticatedUserIWantToProcessACsvFileWhichHasMissingMetadataForOneCaseAndValidMetadataForAnother()
        throws IOException, InterruptedException {
        if (getMetadataMigrationEnabled()) {
            String document1Url = createDocumentAndGetUrlAs(getCitizen());
            String[] split = document1Url
                .split("/");
            String document1Id = split[split.length - 1];
            String document2Url = createDocumentAndGetUrlAs(getCitizen());
            split = document2Url
                .split("/");
            String document2Id = split[split.length - 1];

            // missing metadata in the CSV file
            String body = "id,case_id,case_type_id,jurisdiction,document_id,document_url,case_created_date,"
                + "case_last_modified_date,migrated\n"
                + "1,,,," + document1Id + URL_WITH_COMMA
                + document1Id + ",2020-02-28 14:51:10.592,2020-02-28 14:51:10.6,f\n"
                + "2,2,AAT,AUTOTEST2," + document2Id + URL_WITH_COMMA + document2Id + DATE_TIME_WITH_TEXT;

            File file = File.createTempFile(MIGRATION_CONST, ".csv");
            try (FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.write(body);
            }

            postCsvFileAndTriggerSpringBatchJob(file);

            Response metadata1 = fetchDocumentMetaDataAs(getCitizen(), document1Url);
            Object caseId1 = metadata1.body().prettyPeek().jsonPath().get(METADATA_CASE_ID_CONST);
            Object caseTypeId1 = metadata1.body().prettyPeek().jsonPath().get(METADATA_CASE_TYPE_ID_CONST);
            Object jurisdiction1 = metadata1.body().prettyPeek().jsonPath().get(METADATA_JURISDICTION_CONST);

            Response metadata2 = fetchDocumentMetaDataAs(getCitizen(), document2Url);
            Object caseId2 = metadata2.body().prettyPeek().jsonPath().get(METADATA_CASE_ID_CONST);
            Object caseTypeId2 = metadata2.body().prettyPeek().jsonPath().get(METADATA_CASE_TYPE_ID_CONST);
            Object jurisdiction2 = metadata2.body().prettyPeek().jsonPath().get(METADATA_JURISDICTION_CONST);

            MatcherAssert.assertThat(caseId1, Matchers.is(""));
            MatcherAssert.assertThat(caseTypeId1, Matchers.is(""));
            MatcherAssert.assertThat(jurisdiction1, Matchers.is(""));

            MatcherAssert.assertThat(caseId2, Matchers.is("2"));
            MatcherAssert.assertThat(caseTypeId2, Matchers.is("AAT"));
            MatcherAssert.assertThat(jurisdiction2, Matchers.is("AUTOTEST2"));
        }

    }

    @Test
    @Pending
    public void asAnAuthenticatedUserIWantToProcessACsvFileWhichHasRandomDocidGeneratedAndMetadataShouldNotBeUpdated()
        throws IOException, InterruptedException {
        if (getMetadataMigrationEnabled()) {
            String document1Url = createDocumentAndGetUrlAs(getCitizen());

            // random DocumentId in CSV File
            String body = "1,1,AAT,AUTOTEST1,randomDocumentId,"
                 + "http://dm-store:8080/documents/randomDocumentId,2020-02-28 14:51:10.592,2020-02-28 14:51:10.6,f";

            File file = File.createTempFile(MIGRATION_CONST, ".csv");
            try (FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.write(body);
            }

            postCsvFileAndTriggerSpringBatchJob(file);

            Response metadata1 = fetchDocumentMetaDataAs(getCitizen(), document1Url);
            Object caseId1 = metadata1.body().prettyPeek().jsonPath().get(METADATA_CASE_ID_CONST);
            Object caseTypeId1 = metadata1.body().prettyPeek().jsonPath().get(METADATA_CASE_TYPE_ID_CONST);
            Object jurisdiction1 = metadata1.body().prettyPeek().jsonPath().get(METADATA_JURISDICTION_CONST);

            assertNull(caseId1);
            assertNull(caseTypeId1);
            assertNull(jurisdiction1);
        }

    }

    public void postCsvFileAndTriggerSpringBatchJob(File file) throws InterruptedException {
        givenRequest(getCitizen())
            .multiPart("files", file, "text/csv")
            .multiPart("classification", String.valueOf(Classifications.PUBLIC))
            .multiPart("roles", "citizen")
            .multiPart("roles", "caseworker")
            .expect()
            .statusCode(200)
            .when()
            .post("/testing/metadata-migration-csv");
        // let the job run
        Thread.sleep(15000);
    }

}
