package uk.gov.hmcts.dm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.dm.client.EmNpaApi;

@Service
public class EmNpaService {

    private static final Logger log = LoggerFactory.getLogger(EmNpaService.class);

    private final EmNpaApi emNpaApi;

    public EmNpaService(EmNpaApi emNpaApi) {
        this.emNpaApi = emNpaApi;
    }

    public boolean deleteRedactionsForDocument(String documentId, String userToken, String serviceToken) {
        log.info("Deleting redactions for document: {}", documentId);
        try {
            ResponseEntity<Void> response = emNpaApi.deleteRedactions(documentId, userToken, serviceToken);
            boolean isNoContent = response != null && response.getStatusCode() == HttpStatus.NO_CONTENT;
            if (!isNoContent) {
                log.error("Em-NPA delete endpoint returned non-204 response for documentId: {}. Status: {}",
                    documentId,
                    response != null ? response.getStatusCode() : null);
            }
            log.info("Successfully deleted redactions for document: {}", documentId);
            return isNoContent;
        } catch (Exception e) {
            log.error("Failed to delete redactions for document: {}", documentId, e);
            throw e;
        }
    }
}
