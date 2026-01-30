package uk.gov.hmcts.dm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.dm.client.EmAnnoApi;

@Service
public class EmAnnoService {

    private static final Logger log = LoggerFactory.getLogger(EmAnnoService.class);

    private final EmAnnoApi emAnnoApi;

    public EmAnnoService(EmAnnoApi emAnnoApi) {
        this.emAnnoApi = emAnnoApi;
    }

    public boolean deleteDocumentData(String docId, String userToken, String serviceToken) {
        log.info("Deleting document data for docId: {}", docId);
        try {
            ResponseEntity<Void> response = emAnnoApi.deleteDocumentData(docId, userToken, serviceToken);
            boolean isNoContent = response != null && response.getStatusCode() == HttpStatus.NO_CONTENT;
            if (!isNoContent) {
                log.error("Em-Anno delete endpoint returned non-204 response for docId: {}. Status: {}",
                    docId,
                    response != null ? response.getStatusCode() : null);
            }
            log.info("Successfully deleted document data for docId: {}", docId);
            return isNoContent;
        } catch (Exception e) {
            log.error("Failed to delete document data for docId: {}", docId, e);
            throw e;
        }
    }
}
