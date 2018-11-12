package uk.gov.hmcts.dm.dialect;

import lombok.EqualsAndHashCode;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;

@EqualsAndHashCode(of = {"contentLength"})
public class PassThroughBlob implements Blob {

    private InputStream binaryStream;

    private Long contentLength;

    public static PassThroughBlob getInstance(MultipartFile file) throws IOException {
        try (final InputStream inputStream = file.getInputStream()) {
            return new PassThroughBlob(inputStream, file.getSize());
        }
    }

    public PassThroughBlob(InputStream binaryStream) {
        this.binaryStream = binaryStream;
    }

    public PassThroughBlob(InputStream binaryStream, Long contentLength) {
        this(binaryStream);
        this.contentLength = contentLength;
    }

    @Override
    public long length() throws SQLException {
        return this.contentLength;
    }

    @Override
    public InputStream getBinaryStream() throws SQLException {
        return binaryStream;
    }

    @Override
    public InputStream getBinaryStream(long pos, long length) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public OutputStream setBinaryStream(long pos) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] getBytes(long pos, int length) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int setBytes(long pos, byte[] bytes) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int setBytes(long pos, byte[] bytes, int offset, int len) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public long position(byte[] pattern, long start) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public long position(Blob pattern, long start) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void truncate(long len) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void free() throws SQLException {
        throw new UnsupportedOperationException();
    }


}
