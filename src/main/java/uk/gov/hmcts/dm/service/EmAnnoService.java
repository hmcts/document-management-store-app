package uk.gov.hmcts.dm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.dm.client.EmAnnoApi;

@Service
public class EmAnnoService {

    private static final Logger log = LoggerFactory.getLogger(EmAnnoService.class);

    private final EmAnnoApi emAnnoApi;

    public EmAnnoService(EmAnnoApi emAnnoApi) {
        this.emAnnoApi = emAnnoApi;
    }

    public void deleteDocumentData(String docId, String userToken, String serviceToken) {
        log.info("Deleting document data for docId: {}", docId);
        try {
            emAnnoApi.deleteDocumentData(docId, userToken, serviceToken);
            log.info("Successfully deleted document data for docId: {}", docId);
        } catch (Exception e) {
            log.error("Failed to delete document data for docId: {}", docId, e);
            throw e;
        }
    }
}
