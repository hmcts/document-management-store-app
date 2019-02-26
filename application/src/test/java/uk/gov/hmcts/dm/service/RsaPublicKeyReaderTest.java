package uk.gov.hmcts.dm.service;

import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.KeySpec;
import java.security.spec.RSAPublicKeySpec;

import static javax.crypto.Cipher.DECRYPT_MODE;
import static javax.crypto.Cipher.ENCRYPT_MODE;
import static org.apache.commons.codec.binary.Base64.encodeBase64;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.jclouds.crypto.Pems.privateKeySpec;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.dm.service.BatchMigrationTokenService.SSH_ALGORITHM;

public class RsaPublicKeyReaderTest {

    protected static final String PUBLIC_KEY_STRING = "ssh-rsa "
        + "AAAAB3NzaC1yc2EAAAADAQABAAABAQDiQ//gc/G53d9dLCtf123fIYo49gUySuJuxOcw2GtieWTMSy"
        + "+O7RNtsAIjVf3mCOdDNuN69tZNPEWMdaW8n11s9MwYFahtxDecyn0KIP9MvPsfSMSbxhp/f7kfbdB/H"
        + "/S5eYea66JTyeJS6uNd76RdHttx0mLO30ZkRcXB25c2SIXhRYsdoeKS5GXHDdNejkQM0S"
        + "/Ev94x2UunApmYHjWN1XcDhsEsAeF4WHnvYh2XiMn9vHY44AqvbWLlAmCgzaXpz8Xhl0fO7jDKSeReDyuM3UTMaiFFaxuvliGol7aIXq/aVe"
        + "/miiD2SLxHZ6RxAPW80bhXrzJMTLTCqhCEhzfv devel@ell,"
        + "lake.67842b95c6cd11d892e2080020a0f4c9";
    private static final String PRIVATE_KEY_STRING = "-----BEGIN RSA PRIVATE KEY-----\n"
        + "MIIEpAIBAAKCAQEA4kP/4HPxud3fXSwrX9dt3yGKOPYFMkribsTnMNhrYnlkzEsv\n"
        + "ju0TbbACI1X95gjnQzbjevbWTTxFjHWlvJ9dbPTMGBWobcQ3nMp9CiD/TLz7H0jE\n"
        + "m8Yaf3+5H23Qfx/0uXmHmuuiU8niUurjXe+kXR7bcdJizt9GZEXFwduXNkiF4UWL\n"
        + "HaHikuRlxw3TXo5EDNEvxL/eMdlLpwKZmB41jdV3A4bBLAHheFh572Idl4jJ/bx2\n"
        + "OOAKr21i5QJgoM2l6c/F4ZdHzu4wyknkXg8rjN1EzGohRWsbr5YhqJe2iF6v2lXv\n"
        + "5oog9ki8R2ekcQD1vNG4V68yTEy0wqoQhIc37wIDAQABAoIBAQCUN/0/WFRp8Ejo\n"
        + "rQ2AzAuCVmNIawj+aAWqkDvuSGcX6/O2zC3MT8shWGIeKa+X6A8ufZ9Ipre9gpUA\n"
        + "rPSz+iVmQXoM8OsTdK0FfbOpVfJHn6hVVwLMFFq5WL65MuOY0yMp+M13E5KTlrU2\n"
        + "of3tRBWElrkj97CYWbSv7PDdy+5jNKVLmLmBwmGTbh9US+7bZ+dkLZlLL0MV7IqM\n"
        + "68cnkePA5m8JduP8LwkKq88M84FqQ3kaiTYRUpoA4+kYv1YFN1EP7Awgy8wMx1/g\n"
        + "lR+aXcTc+If4XIj+K+e3KvcDZKrempQ4KIkElBrqE7jFbGxNvPZVlIHohvzTt2ZK\n"
        + "nroqC5lJAoGBAPnw938mTw0b2OLTgUCiLW7yb19I2z5s1VTw8hMffBj0Fs6GCRrN\n"
        + "LJc9MtqIx2ANEUVwEwzAI6RZbkDRmgOcFwc4XIniIgL+fnDnDL5YMuFjIeyKuYaa\n"
        + "E4J9tf/uOUjShJUUvQr7bBIwwhpuF4rao12ZI4Q56yq6Srx58vbJowXbAoGBAOfA\n"
        + "HH/ECr0NqulUHoevLW6dkcQ4ICVOIFPAAMU69opsZordyF7KGjvFurNj8Q2vjUHE\n"
        + "XvWqdr5LRopxenBS0MKhjtTCqkgDT5AiwFpBoEIbxRtMAkcuqs03Ulpf+g6LWDlq\n"
        + "5o5XEmWg3gD7jTYDJeelxX+SlShnmmEskcp7fNR9AoGBAJp6vt3NlapqfcCkLQz/\n"
        + "r8kU+pUP+MI6jhgz28rJ+O+LXdVDrrjr+xV288lyJJhPO0+Jl5yX3O+lfql9Yw28\n"
        + "GdeSlE98fr0PAqNRiuTSqo/3r239x/FTpZ9Ph3+pg6powx1JdhYlk4QisrWXOeHO\n"
        + "hI5DKlp9WcZM4ghf7zVtJt9lAoGAdG6iYtZ9hqn9OijXBdhO/QoRGAIStGth9nG6\n"
        + "cVzETJtVkWMHrgff0jPvLv2BOB0A3Q/pnYc9DTIIiLmmiBQzafV9Kwt6PZ/cM1Ai\n"
        + "T7Y068HD74jqhBTAH0YyC9G4ceI0OvSoTM8WIRUHNHstYPuH6a/xX7ynT5tthnC7\n"
        + "9nzAJIkCgYBg41OqziHmNd+9nhhxVyYs7+0l48+nyVDRbvqX6jdvDIG7iu0ERpLV\n"
        + "56EJZMpbmeidFNymPaigTQuNENSmhGGswWRfMwCWTRIu+cvfPQjyWvqnsj2aHIQA\n"
        + "DkvPBLopLN3KYZ9OQGvZitfVzvN5l63XCbioWYNdr2vej3cPT8dNLQ==\n"
        + "-----END RSA PRIVATE KEY-----";

    private String messageUnderTest;

    private RsaPublicKeyReader publicKeyReader;

    @Before
    public void setUp() {
        publicKeyReader = new RsaPublicKeyReader();
        messageUnderTest = randomAlphanumeric(nextInt(8, 200));
    }

    @Test
    public void encryptWithPublicKey() throws Exception {
        final Cipher cipher = Cipher.getInstance(SSH_ALGORITHM);

        final RSAPublicKeySpec rsaPublicKeySpec = publicKeyReader.parsePublicKey(PUBLIC_KEY_STRING);
        cipher.init(ENCRYPT_MODE, KeyFactory.getInstance(SSH_ALGORITHM).generatePublic(rsaPublicKeySpec));

        final String val1 = new String(encodeBase64(cipher.doFinal(messageUnderTest.getBytes())));
        final String val2 = new String(encodeBase64(cipher.doFinal(messageUnderTest.getBytes())));

        // checks that encrypted strings are not equal
        assertThat(val1, not(val2));

        final KeySpec privateKeySpec = privateKeySpec(PRIVATE_KEY_STRING);
        cipher.init(DECRYPT_MODE, KeyFactory.getInstance(SSH_ALGORITHM).generatePrivate(privateKeySpec));

        final String decrypted1 = new String(cipher.doFinal(Base64.decodeBase64(val1)), "UTF-8");
        final String decrypted2 = new String(cipher.doFinal(Base64.decodeBase64(val2)), "UTF-8");

        // checks the we have the original string
        assertThat(decrypted1, is(messageUnderTest));
        assertThat(decrypted2, is(messageUnderTest));
    }

    @Test
    public void decryptWithPublicKey() throws Exception {
        final Cipher cipher = Cipher.getInstance(SSH_ALGORITHM);

        final KeySpec privateKeySpec = privateKeySpec(PRIVATE_KEY_STRING);
        cipher.init(ENCRYPT_MODE, KeyFactory.getInstance(SSH_ALGORITHM).generatePrivate(privateKeySpec));

        final String val = new String(encodeBase64(cipher.doFinal(messageUnderTest.getBytes())));
        final RSAPublicKeySpec rsaPublicKeySpec = publicKeyReader.parsePublicKey(PUBLIC_KEY_STRING);
        final PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(rsaPublicKeySpec);

        cipher.init(DECRYPT_MODE, KeyFactory.getInstance(SSH_ALGORITHM).generatePublic(rsaPublicKeySpec));
        cipher.init(DECRYPT_MODE, publicKey);

        final String decrypted = new String(cipher.doFinal(Base64.decodeBase64(val)), "UTF-8");

        // checks the we have the original string
        assertThat(decrypted, is(messageUnderTest));
    }

    @Test(expected = BadPaddingException.class)
    public void encryptAndDecryptWithSamePublicKey() throws Exception {
        final Cipher cipher = Cipher.getInstance(SSH_ALGORITHM);

        final RSAPublicKeySpec rsaPublicKeySpec = publicKeyReader.parsePublicKey(PUBLIC_KEY_STRING);
        cipher.init(ENCRYPT_MODE, KeyFactory.getInstance(SSH_ALGORITHM).generatePublic(rsaPublicKeySpec));

        final String val = new String(encodeBase64(cipher.doFinal(messageUnderTest.getBytes())));

        final Cipher cipher2 = Cipher.getInstance(SSH_ALGORITHM);
        cipher2.init(DECRYPT_MODE, KeyFactory.getInstance(SSH_ALGORITHM).generatePublic(rsaPublicKeySpec));

        cipher2.doFinal(Base64.decodeBase64(val));
    }

    @Test(expected = RuntimeException.class)
    public void badPublicKey1() throws Exception {
        publicKeyReader.parsePublicKey("Part1 Part2 devel@ell.lake");
    }

    @Test(expected = RuntimeException.class)
    public void badPublicKey2() throws Exception {
        publicKeyReader.parsePublicKey("ARandomString");
    }

    @Test(expected = RuntimeException.class)
    public void badSshPublicKey() throws Exception {
        publicKeyReader.parsePublicKey("ssh-rsa Exptected to fail devel@ell.lake");
    }

    @Test(expected = BadPaddingException.class)
    public void keyPairNotMatched() throws Exception {
        final String anotherPrivateKey = "-----BEGIN RSA PRIVATE KEY-----\n"
            + "MIIEogIBAAKCAQEAtjIzlHw8c+/Z75ftVLTdbNBO9u4xb6FM/nqYL3VkEX5A4u/+\n"
            + "umx5QhkfGZR7YrHUzj5hUnTxO409vrVD8qgkxUs2zhzVmBpdaRNV45iK0M2FiKzB\n"
            + "CICwvN+KTKSVGg7e8vJw7CXYxaVDB3Nyvu23OmvFuoNdlPl4vFxpPl4zhvEZVa51\n"
            + "um1VhPAZ65ma9QMxZtuL/MyvXnnSp6NwOBMDkmCh1Fhye1/0ndGtcna4VSqZzrZh\n"
            + "3Jy6D1PmUaSKMWzAQ5Gq9OlR8F3kf+T4x0cdNEX/SiHC8YroM/xCeOZkyxkcx5Up\n"
            + "7RBZqtHucqSCDr/EbAgbREGfAOg6JaP4Ut9bcwIDAQABAoIBABjObv1piOWUI40e\n"
            + "l16wHXHCTu4J8ksroAsJ8AyOJCjJktYTLRB+0mewee6Gq+H9KmdGUQPkDjA4g86i\n"
            + "aXTxdcgy/GxFusMx28QBsPEDBOt3ABZixwl8VBPzL6ZNxks8+RgE8GEGqiQG40Bx\n"
            + "dI8QWKEoqUxCtIwjB1BGGMI4GzXlBJLVdhjDtQsskSqPv2D9dCo7qPapb4neMkUl\n"
            + "9W4x9lwLsUqurJNjOLOa5fU6l6CLQegq0Q+2317w+0ca0AO90ixMdWUE/rAXt3fj\n"
            + "1hylfh4DmfXtkmyhe0BYui0QKxAnAUEmrLwrBIXSx/0vjOb2tyF5rkHS/bn2eBdd\n"
            + "LTx3htkCgYEA8HKAGCRGsd0YQ18I3xT0Ymiy/fXNE0SzeXrN0pMj1hWaDcJwEOQb\n"
            + "jTW3T+nJSJJRR/wcormQsjnYIxmSZwIneV5hmRJhBqoBkL/FRdPIpEkJvWlgh35G\n"
            + "vpHQMSws77Hks2fF/iRnSZFQkLQ74dE+alwOalflJHwDC0B6SieP048CgYEAwfsi\n"
            + "3Ofk4zUJjERpqPKJ+ElcE4Mg07FolfWK3KvsYPxNAnU7sHUpJV4SsGL96nbEP5BO\n"
            + "Xydo5boaaVTCFtUEU1CG+voErFSM8MC/Cu3h1Q5q0l+xPelp+rOlGGRiPIqdLOuP\n"
            + "T4HcXorZtTEJvvPP2DwxIK9F+EwGuFpwf52eN90CgYA2dQhT72tffz6ui4ib1cgf\n"
            + "vazEXfZLZeTsH4ccKR0VsKDKev4cpT0kN0VEQSPIXvMvxYbKyBJgez9be8Avukos\n"
            + "b9bU71ewRbLDENviG7w2kSA2mPY22b+2C5pGeMXYp8avZ1JtY9tMyy+fBmMFmvly\n"
            + "51SQe0W3hKq/0qOfxeQKOwKBgDV52UOS/iuQzIAJwCMeMeDrwGF0rdEOQGnO1ig9\n"
            + "kR2vkD9UOE9ODTjK9VYBBETl1vY6/xAGWkmyg2N0RUlsEhQxx/Rahn1Hlc8tI72M\n"
            + "lMxafCJg1QjfVyMQ8TuHOPm9sNTYX5qojyrVFm14BlrI9gyDk3uSNjTcKKIb9JWJ\n"
            + "yztVAoGAcoN7PT2JUfKRLz3At+XsTwf/t2JUDLbxIEh0EYIeo9BOeeOaAX8pyNqs\n"
            + "ZTzeQm4irdGfST488PwJCxvo1uCJfQf6LN81Eig8vrzVjzID2+JJdfn6SehSIRy5\n"
            + "6nFb6/MFit4okDPLitqhOY09YHsU124tuQ7rWjpF4oH922OM/fE=\n"
            + "-----END RSA PRIVATE KEY-----";

        final Cipher cipher = Cipher.getInstance(SSH_ALGORITHM);

        final KeySpec privateKeySpec = privateKeySpec(anotherPrivateKey);
        cipher.init(ENCRYPT_MODE, KeyFactory.getInstance(SSH_ALGORITHM).generatePrivate(privateKeySpec));

        final String val = new String(encodeBase64(cipher.doFinal("simple".getBytes())));

        final RSAPublicKeySpec rsaPublicKeySpec = publicKeyReader.parsePublicKey(PUBLIC_KEY_STRING);
        cipher.init(DECRYPT_MODE, KeyFactory.getInstance(SSH_ALGORITHM).generatePublic(rsaPublicKeySpec));

        cipher.doFinal(Base64.decodeBase64(val));
    }
}
