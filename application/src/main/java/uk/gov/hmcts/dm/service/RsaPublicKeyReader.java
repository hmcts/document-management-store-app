package uk.gov.hmcts.dm.service;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.spec.RSAPublicKeySpec;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.size;
import static com.google.common.io.BaseEncoding.base64;
import static org.jclouds.util.Strings2.toStringAndClose;

@Component
class RsaPublicKeyReader {

    RSAPublicKeySpec parsePublicKey(@NotNull String idRsaPub) throws IOException {
        return parsePublicKey(ByteSource.wrap(idRsaPub.getBytes(Charsets.UTF_8)));
    }

    private RSAPublicKeySpec parsePublicKey(ByteSource supplier) throws IOException {
        InputStream stream = supplier.openStream();
        Iterable<String> parts = Splitter.on(' ').split(toStringAndClose(stream).trim());
        checkArgument(size(parts) >= 2 && "ssh-rsa".equals(get(parts, 0)), "bad format, should be: ssh-rsa AAAAB3...");
        stream = new ByteArrayInputStream(base64().decode(get(parts, 1)));
        String marker = new String(readLengthFirst(stream));
        checkArgument("ssh-rsa".equals(marker), "looking for marker ssh-rsa but got %s", marker);
        BigInteger publicExponent = new BigInteger(readLengthFirst(stream));
        BigInteger modulus = new BigInteger(readLengthFirst(stream));
        return new RSAPublicKeySpec(modulus, publicExponent);
    }

    // @See http://www.ietf.org/rfc/rfc4253.txt
    private static byte[] readLengthFirst(InputStream in) throws IOException {
        int byte1 = in.read();
        int byte2 = in.read();
        int byte3 = in.read();
        int byte4 = in.read();
        int length = (byte1 << 24) + (byte2 << 16) + (byte3 << 8) + byte4;
        byte[] val = new byte[length];
        ByteStreams.readFully(in, val);
        return val;
    }
}
