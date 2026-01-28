package uk.gov.hmcts.dm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import java.util.UUID;

@Service
@ConditionalOnProperty(value = "toggle.deletemetadatafordocument", havingValue = "true")
public class DocumentMetadataDeletionService {

    private static final Logger log = LoggerFactory.getLogger(DocumentMetadataDeletionService.class);

    private final EmAnnoService emAnnoService;
    private final EmNpaService emNpaService;
    private final AuthTokenGenerator authTokenGenerator;
    private final IdamClient idamClient;
    private final String systemUsername;
    private final String systemPassword;

    public DocumentMetadataDeletionService(
        EmAnnoService emAnnoService,
        EmNpaService emNpaService,
        AuthTokenGenerator authTokenGenerator,
        IdamClient idamClient,
        @Value("${idam.system-user.username}") String systemUsername,
        @Value("${idam.system-user.password}") String systemPassword
    ) {
        this.emAnnoService = emAnnoService;
        this.emNpaService = emNpaService;
        this.authTokenGenerator = authTokenGenerator;
        this.idamClient = idamClient;
        this.systemUsername = systemUsername;
        this.systemPassword = systemPassword;
    }

    /**
     * Deletes metadata from external systems (em-anno and em-npa) for a given document.
     * Process:
     * 1. Generate authentication tokens (IDAM user token and S2S service token)
     * 2. Call em-anno delete endpoint first
     * 3. If successful (204), call em-npa delete endpoint
     * 4. Only return true if both calls succeed (204)
     *
     * @param documentId the UUID of the document
     * @return true if both deletions succeeded, false otherwise
     */
    public boolean deleteExternalMetadata(UUID documentId) {
        String docId = documentId.toString();

        try {
            // Generate authentication tokens once for both service calls
            String userToken = generateUserToken();
            String serviceToken = generateServiceToken();

            // Step 1: Delete from em-anno first
            log.info("Attempting to delete metadata from em-anno for document: {}", docId);
            emAnnoService.deleteDocumentData(docId, userToken, serviceToken);
            log.info("Successfully deleted metadata from em-anno for document: {}", docId);

            // Step 2: Delete from em-npa
            log.info("Attempting to delete redactions from em-npa for document: {}", docId);
            emNpaService.deleteRedactionsForDocument(docId, userToken, serviceToken);
            log.info("Successfully deleted redactions from em-npa for document: {}", docId);

            return true;

        } catch (Exception e) {
            log.error("Failed to delete external metadata for document: {}. Error: {}",
                docId, e.getMessage(), e);
            return false;
        }
    }

    private String generateUserToken() {
        log.debug("Generating IDAM user token for system user");
        String tokenResponse = idamClient.getAccessToken(systemUsername, systemPassword);
        return tokenResponse;
    }

    private String generateServiceToken() {
        return authTokenGenerator.generate();
    }
}
