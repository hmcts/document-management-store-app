package uk.gov.hmcts.reform.dm.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.dm.config.V1MediaType;
import uk.gov.hmcts.reform.dm.domain.StoredDocument;
import uk.gov.hmcts.reform.dm.domain.StoredDocumentAuditEntry;
import uk.gov.hmcts.reform.dm.exception.StoredDocumentNotFoundException;
import uk.gov.hmcts.reform.dm.hateos.StoredDocumentAuditEntryHalResource;
import uk.gov.hmcts.reform.dm.service.AuditEntryService;
import uk.gov.hmcts.reform.dm.service.StoredDocumentService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by pawel on 28/07/2017.
 */
@RestController
@RequestMapping(path = "/documents")
@Api("Endpoint for Audit entities.")
public class StoredDocumentAuditController {

    @Autowired
    private AuditEntryService auditEntryService;

    @Autowired
    private StoredDocumentService storedDocumentService;

    @GetMapping("{id}/auditEntries")
    @ApiOperation("Retrieves audits related to a Stored Document.")
    public ResponseEntity<Object> findAudits(@PathVariable UUID id) {
        StoredDocument storedDocument = storedDocumentService.findOne(id);

        if (storedDocument == null) {
            throw new StoredDocumentNotFoundException(String.format("ID: %s", id.toString()));
        }

        List<StoredDocumentAuditEntry> auditEntries = auditEntryService.findStoredDocumentAudits(storedDocument);

        Resources<StoredDocumentAuditEntryHalResource> resources = new Resources<>(auditEntries
                .stream()
                .map(StoredDocumentAuditEntryHalResource::new).collect(Collectors.toList()));

        return ResponseEntity.ok().contentType(V1MediaType.V1_HAL_AUDIT_ENTRY_COLLECTION_MEDIA_TYPE).body(resources);
    }

}
