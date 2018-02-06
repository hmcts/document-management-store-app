package uk.gov.hmcts.dm.dialect;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.InputStream;

/**
 * Created by pawel on 06/02/2018.
 */
public class PassThroughBlobTests {

    @Test(expected = UnsupportedOperationException.class)
    public void test_getBinaryStream() throws Exception {
        InputStream in = Mockito.mock(InputStream.class);
        PassThroughBlob blob = new PassThroughBlob(in);
        blob.getBinaryStream(0, 0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void test_setBinaryStream() throws Exception {
        InputStream in = Mockito.mock(InputStream.class);
        PassThroughBlob blob = new PassThroughBlob(in);
        blob.setBinaryStream(0l);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void test_getBytes() throws Exception {
        InputStream in = Mockito.mock(InputStream.class);
        PassThroughBlob blob = new PassThroughBlob(in);
        blob.getBytes(0l, 1);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void test_setBytes() throws Exception {
        InputStream in = Mockito.mock(InputStream.class);
        PassThroughBlob blob = new PassThroughBlob(in);
        blob.setBytes(0l, null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void test_setBytesOverloaded() throws Exception {
        InputStream in = Mockito.mock(InputStream.class);
        PassThroughBlob blob = new PassThroughBlob(in);
        blob.setBytes(0l, null, 0, 0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void test_Position() throws Exception {
        InputStream in = Mockito.mock(InputStream.class);
        PassThroughBlob blob = new PassThroughBlob(in);
        blob.position(new byte[] {  }, 0l);
    }


    @Test(expected = UnsupportedOperationException.class)
    public void test_PositionBlob() throws Exception {
        InputStream in = Mockito.mock(InputStream.class);
        PassThroughBlob blob = new PassThroughBlob(in);
        blob.position(new PassThroughBlob(in), 0l);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void test_truncate() throws Exception {
        InputStream in = Mockito.mock(InputStream.class);
        PassThroughBlob blob = new PassThroughBlob(in);
        blob.truncate(0l);
    }


    @Test(expected = UnsupportedOperationException.class)
    public void test_free() throws Exception {
        InputStream in = Mockito.mock(InputStream.class);
        PassThroughBlob blob = new PassThroughBlob(in);
        blob.free();
    }


    @Test(expected = UnsupportedOperationException.class)
    public void test_hashCode() throws Exception {
        InputStream in = Mockito.mock(InputStream.class);
        PassThroughBlob blob = new PassThroughBlob(in, 1l);
        Assert.assertEquals(new Long(1l).hashCode(), blob.hashCode());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void test_Equals() throws Exception {
        InputStream in = Mockito.mock(InputStream.class);
        PassThroughBlob blob = new PassThroughBlob(in, 1l);
        PassThroughBlob blob2 = new PassThroughBlob(in, 1l);
        Assert.assertTrue(blob.equals(blob2));
    }
}
