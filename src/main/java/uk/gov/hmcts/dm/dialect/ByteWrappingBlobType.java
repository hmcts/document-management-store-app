package uk.gov.hmcts.dm.dialect;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.UserType;

import java.io.InputStream;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Created by pawel on 05/02/2018.
 */
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
    public boolean equals(Object x, Object y) throws HibernateException {
        return x == y;
    }

    @Override
    public int hashCode(Object x) throws HibernateException {
        return x.hashCode();
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
        InputStream inputStream = rs.getBinaryStream(names[0]);
        return new PassThroughBlob(inputStream, 0);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session) throws HibernateException, SQLException {
        try {
            PassThroughBlob b = (PassThroughBlob)value;
            st.setBinaryStream(index, b.getBinaryStream(), b.length());
            System.out.println("A");
        } catch (Throwable e) {
            throw new HibernateException(e);
        }

    }

    @Override
    public Object deepCopy(Object value) throws HibernateException {
        return value;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        return null;
    }

    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return null;
    }

    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return null;
    }

//    public void set(PreparedStatement st, Blob blob, int index,
//                    SessionImplementor session) throws SQLException {
//        // we are using the useInputStreamToInsertBlob() as a proxy for mapping blobs to binaries
//        String mapProp = session.getFactory().getDialect().getDefaultProperties().getProperty(MAP_BLOBS_TO_BINARY_TYPE);
//        final boolean mapBlobToStreams = String.valueOf(true).equals(mapProp);
//
//        // if we are setting a NULL value, it's important that we know what the real DB type is
//        // if we are mapping to a binary type, then use that type to set null
//        if (blob == null) {
//            st.setNull(index, mapBlobToStreams ? Types.BINARY : Types.BLOB);
//        } else {
//            if (mapBlobToStreams) {
//                st.setBinaryStream( index, blob.getBinaryStream(), (int) blob.length() );
//            }
//            else {
//                super.set(st, blob, index, session);
//            }
//        }
//    }
//
//    /**
//     * on calling get(), let's just call getObject() and see what happens, then turn it
//     * into a blob if it isn't one.
//     */
//    public Object get(ResultSet rs, String name) throws HibernateException,	SQLException {
//        Object value = rs.getObject(name);
//
//        if (rs.wasNull()) {
//            return null;
//        }
//        if (value instanceof Blob) {
//            //return new SerializableBlob((Blob) value);
//
//        } else if (value instanceof byte[]) {
//            //return Hibernate.getLobCreator().createBlob((byte[]) value);
//        } else if (value instanceof InputStream) {
//            try {
//                //return Hibernate.createBlob((InputStream) value);
//            } catch (Exception e) {
//                throw new HibernateException("exception creating blob from input stream", e);
//            }
//        } else {
//            throw new HibernateException("I don't know how to map the type " + value.getClass().getName() + " to a blob");
//        }
//        return null;
//    }

}
