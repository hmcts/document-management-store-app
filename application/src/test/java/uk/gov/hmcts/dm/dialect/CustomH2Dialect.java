package uk.gov.hmcts.dm.dialect;

import java.sql.Types;

import org.hibernate.dialect.H2Dialect;

public class CustomH2Dialect extends H2Dialect {

    public CustomH2Dialect() {
        super();
        // use "bytea" to map blob types
        registerColumnType(Types.BLOB, "bytea");
        // turn on blob mapping
        getDefaultProperties().setProperty(ByteWrappingBlobType.MAP_BLOBS_TO_BINARY_TYPE, String.valueOf(true));
    }
}
