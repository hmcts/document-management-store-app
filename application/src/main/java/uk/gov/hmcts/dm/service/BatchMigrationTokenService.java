package uk.gov.hmcts.dm.service;

import com.google.common.annotations.VisibleForTesting;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.dm.exception.ValidationErrorException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;

import static javax.crypto.Cipher.DECRYPT_MODE;
import static org.apache.commons.lang.StringUtils.isBlank;

@Service
@Slf4j
class BatchMigrationTokenService {

    protected static final String SSH_ALGORITHM = "RSA";

    private final RsaPublicKeyReader sshPublicKeyParser;

    @VisibleForTesting
    @Value("${blobstore.migrate.ccd.secret:y2hahvdZ9evcTVq2}")
    @Setter
    private String migrateSecret;

    @VisibleForTesting
    @Value("${blobstore.migrate.ccd.publicKey}")
    @Setter
    private String migrationPublicKeyStringValue;

    @VisibleForTesting
    @Setter
    @Value("${blobstore.migrate.ccd.publicKeyRequired:false}")
    private boolean publicKeyRequired;

    @Autowired
    protected BatchMigrationTokenService(final RsaPublicKeyReader sshPublicKeyParser) {
        this.sshPublicKeyParser = sshPublicKeyParser;
    }

    protected void checkAuthToken(String authToken) {
        if (publicKeyRequired) {
            if (isBlank(authToken)) {
                throw new ValidationErrorException("An auth token is expected");
            }

            decrypt(authToken);
        }
    }

    private void decrypt(final String authToken) {
        try {
            final RSAPublicKeySpec rsaPublicKeySpec = sshPublicKeyParser.parsePublicKey(migrationPublicKeyStringValue);
            final Cipher cipher = Cipher.getInstance(SSH_ALGORITHM);
            cipher.init(DECRYPT_MODE, KeyFactory.getInstance(SSH_ALGORITHM).generatePublic(rsaPublicKeySpec));
            final String decryptedSecret = new String(cipher.doFinal(Base64.decodeBase64(authToken)), "UTF-8");
            if (!StringUtils.equals(migrateSecret, decryptedSecret)) {
                throw new ValidationErrorException("Incorrect secret");
            }
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
            | BadPaddingException | InvalidKeySpecException | IOException e) {
            throw new ValidationErrorException(e);
        }
    }
}
