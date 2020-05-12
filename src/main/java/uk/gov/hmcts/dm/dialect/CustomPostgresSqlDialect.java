package uk.gov.hmcts.dm.dialect;

import org.hibernate.dialect.PostgreSQL94Dialect;

import java.sql.Types;

public class CustomPostgresSqlDialect extends PostgreSQL94Dialect {
    public CustomPostgresSqlDialect() {
        super();
        // use "bytea" to map blob types
        registerColumnType(Types.BLOB, "bytea");
        // turn on blob mapping
        getDefaultProperties().setProperty(ByteWrappingBlobType.MAP_BLOBS_TO_BINARY_TYPE, String.valueOf(true));
    }
}
