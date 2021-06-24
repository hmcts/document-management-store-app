package uk.gov.hmcts.dm.dialect;

import org.apache.commons.io.IOUtils;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import java.io.InputStream;
import java.io.Serializable;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class ByteWrappingBlobType implements UserType {

    public static final String MAP_BLOBS_TO_BINARY_TYPE = "mapBlobsToBinaryType";

    @Override
    public int[] sqlTypes() {
        return new int[]{Types.BLOB};
    }

    @Override
    public Class returnedClass() {
        return PassThroughBlob.class;
    }

    @Override
    public boolean equals(Object x, Object y) {
        if (x != null) {
            return x.equals(y);
        } else if (y != null) {
            return y.equals(x);
        } else {
            return false;
        }


    }

    @Override
    public int hashCode(Object x) {
        return x.hashCode();
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner) throws SQLException {
        InputStream inputStream = rs.getBinaryStream(names[0]);
        return new PassThroughBlob(inputStream, 0L);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session) {
        InputStream io = null;
        try {
            Blob b = (Blob) value;
            io = b.getBinaryStream();
            st.setBinaryStream(index, io, b.length());
        } catch (SQLException e) {
            throw new HibernateException("Could not nullSafeSet", e);
        } finally {
            IOUtils.closeQuietly(io);
        }
    }

    @Override
    public Object deepCopy(Object value)  {
        InputStream io = null;
        if (value != null) {
            try {
                Blob blob = (Blob) value;
                io = blob.getBinaryStream();
                return new PassThroughBlob(io, blob.length());
            } catch (SQLException e) {
                throw new HibernateException("Could not deepCopy", e);
            } finally {
                IOUtils.closeQuietly(io);
            }
        }
        return null;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(Object value)  {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object assemble(Serializable cached, Object owner)  {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object replace(Object original, Object target, Object owner)  {
        throw new UnsupportedOperationException();
    }

}
