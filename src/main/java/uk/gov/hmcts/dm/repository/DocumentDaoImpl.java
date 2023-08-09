package uk.gov.hmcts.dm.repository;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.dm.domain.AuditActions;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.DocumentContentVersionAuditEntry;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.domain.mapper.DocumentContentVersionMapper;
import uk.gov.hmcts.dm.domain.mapper.StoredDocumentMapper;

import java.util.Date;
import java.util.UUID;

@Repository
public class DocumentDaoImpl {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    public StoredDocument getStoredDocument(UUID id) {
        try {
            String query = "SELECT * FROM STOREDDOCUMENT WHERE ID = ?";
            StoredDocument storedDocument = jdbcTemplate.queryForObject(
                query, new StoredDocumentMapper(), id);
            return storedDocument;
        } catch (EmptyResultDataAccessException e) {
            return null; //Implies there is no record for given Id.
        }
    }

    //Below will require an Index to added for the storeddocument_id column
    public DocumentContentVersion getRecentDocumentContentVersion(UUID storeddocumentId) {

        //Get only the latest version based on createdOn date with minimum required fields only.

        try {
            String sql = "SELECT * FROM DocumentContentVersion WHERE storeddocument_id = ? ORDER BY createdOn DESC LIMIT 1";
            DocumentContentVersion documentContentVersion = jdbcTemplate.queryForObject(
                sql, new DocumentContentVersionMapper(), storeddocumentId);

            return documentContentVersion;
        } catch (EmptyResultDataAccessException e) {
            return null; //Implies there is no record for given Id.
        }
    }

    public DocumentContentVersionAuditEntry createAndSaveDocumentContentVersionAuditEntry(
        DocumentContentVersion documentContentVersion, String username,
            String serviceName, AuditActions action, UUID documentId) {

        DSLContext dslContext = DSL.using(dbUrl);

        DocumentContentVersionAuditEntry documentContentVersionAuditEntry = new DocumentContentVersionAuditEntry();
        documentContentVersionAuditEntry.setAction(action);
        documentContentVersionAuditEntry.setUsername(username);
        documentContentVersionAuditEntry.setServiceName(serviceName);
        documentContentVersionAuditEntry.setRecordedDateTime(new Date());
        documentContentVersionAuditEntry.setDocumentContentVersion(documentContentVersion);
        documentContentVersionAuditEntry.setStoredDocument(getStoredDocument(documentId));

        return null;
    }


}
