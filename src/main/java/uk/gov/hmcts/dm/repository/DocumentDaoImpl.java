package uk.gov.hmcts.dm.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.dm.domain.AuditActions;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.domain.StoredDocumentAuditEntry;
import uk.gov.hmcts.dm.domain.mapper.StoredDocumentMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public class DocumentDaoImpl {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public StoredDocument getStoredDocument(UUID id) {

        String query = "SELECT * FROM STOREDDOCUMENT WHERE ID = ?";
        StoredDocument storedDocument = jdbcTemplate.queryForObject(
            query, new Object[] { id }, new StoredDocumentMapper());
        //Below will require an Index to added for the storeddocument_id column
        List<DocumentContentVersion> documentContentVersions = getDocumentContentVersions(storedDocument.getId());
        storedDocument.setDocumentContentVersions(documentContentVersions);
        documentContentVersions.forEach(
            documentContentVersion -> documentContentVersion.setStoredDocument(storedDocument)
        );
        //Below will require an Index to added for the storeddocument_id column
        List<StoredDocumentAuditEntry> storedDocumentAuditEntries =
            getStoredDocumentAuditEntries(storedDocument.getId());
        storedDocument.setAuditEntries(new HashSet<>(storedDocumentAuditEntries));
        storedDocumentAuditEntries.forEach(
            storedDocumentAuditEntry -> storedDocumentAuditEntry.setStoredDocument(storedDocument)
        );

        //Below will require an Index to added for the documentroles_id column
        storedDocument.setRoles(new HashSet<>(getDocumentRoles(storedDocument.getId())));

        //Below will require an Index to added for the documentmetadata_id column
        storedDocument.setMetadata(getDocumentMetadata(storedDocument.getId()));

        return storedDocument;
    }

    public List<DocumentContentVersion> getDocumentContentVersions(UUID id) {

        String sql = "SELECT * FROM DocumentContentVersion WHERE storeddocument_id = ?";
        List<DocumentContentVersion> documentContentVersions = jdbcTemplate.query(
            sql, new Object[]{id},
            (rs, rowNum) -> new DocumentContentVersion(
                UUID.fromString(rs.getString("id")),
                rs.getString("mimeType"),
                rs.getString("originalDocumentName"),
                rs.getString("createdBy"),
                rs.getString("createdByService"),
                rs.getDate("createdOn"),
                null,
                null,
                rs.getLong("size"),
                rs.getString("content_uri"),
                rs.getString("content_checksum")
            ));

        return documentContentVersions;
    }

    public List<StoredDocumentAuditEntry> getStoredDocumentAuditEntries(UUID id) {

        String sql = "SELECT * FROM AuditEntry WHERE storeddocument_id = ?";
        List<StoredDocumentAuditEntry> StoredDocumentAuditEntries = jdbcTemplate.query(
            sql, new Object[]{id},
            (rs, rowNum) -> createAndSaveStoredDocumentAuditEntry(rs));

        return StoredDocumentAuditEntries;
    }

    private StoredDocumentAuditEntry createAndSaveStoredDocumentAuditEntry(ResultSet rs) {
        StoredDocumentAuditEntry storedDocumentAuditEntry = new StoredDocumentAuditEntry();
        try {
            storedDocumentAuditEntry.setId(UUID.fromString(rs.getString("id")));
            storedDocumentAuditEntry.setAction(AuditActions.valueOf(rs.getString("action")));
            storedDocumentAuditEntry.setUsername(rs.getString("username"));
            storedDocumentAuditEntry.setServiceName(rs.getString("servicename"));
            storedDocumentAuditEntry.setRecordedDateTime(rs.getDate("recordeddatetime"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return storedDocumentAuditEntry;
    }

    public List<String> getDocumentRoles(UUID id) {

        String sql = "SELECT * FROM DocumentRoles WHERE documentroles_id = ?";

        return jdbcTemplate.query(
            sql, new Object[]{id},
            (rs, rowNum) -> rs.getString("roles"));
    }

    public Map<String,String> getDocumentMetadata(UUID id) {

        String sql = "SELECT * FROM DocumentMetadata WHERE documentmetadata_id = ?";
        HashMap<String,String> toreturn = new HashMap<>();
        jdbcTemplate.query(
            sql, new Object[]{id},
            (rs, rowNum) -> toreturn.put(rs.getString("name"), rs.getString("value")));
        return toreturn;
    }
}
