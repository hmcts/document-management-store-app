package uk.gov.hmcts.dm.domain.mapper;

import org.springframework.jdbc.core.RowMapper;
import uk.gov.hmcts.dm.domain.StoredDocument;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class StoredDocumentMapper implements RowMapper<StoredDocument> {
    @Override
    public StoredDocument mapRow(ResultSet rs, int rowNum) throws SQLException {
        StoredDocument storedDocument =  new StoredDocument(
            UUID.fromString(rs.getString("id")),
            rs.getString("createdBy"),
            rs.getString("createdByService"),
            rs.getString("lastModifiedBy"),
            rs.getString("lastModifiedByService"),
            rs.getDate("modifiedOn"),
            rs.getDate("createdOn"),
            rs.getBoolean("deleted"),
            rs.getBoolean("hardDeleted"),
            null,
            null,
            null,
            null,
            null,
            null,
            rs.getDate("ttl")
        );
        return storedDocument;
    }
}
