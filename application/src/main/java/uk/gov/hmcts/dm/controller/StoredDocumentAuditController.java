package uk.gov.hmcts.dm.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.dm.config.V1MediaType;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.domain.StoredDocumentAuditEntry;
import uk.gov.hmcts.dm.exception.StoredDocumentNotFoundException;
import uk.gov.hmcts.dm.hateos.StoredDocumentAuditEntryHalResource;
import uk.gov.hmcts.dm.repository.StoredDocumentRepository;
import uk.gov.hmcts.dm.service.AuditEntryService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/documents")
@Api("Endpoint for Audit entities.")
public class StoredDocumentAuditController {

    @Autowired
    private AuditEntryService auditEntryService;

    @Autowired
    private StoredDocumentRepository storedDocumentRepository;

    @GetMapping("{documentId}/auditEntries")
    @ApiOperation("Retrieves audits related to a Stored Document.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success", response = StoredDocumentAuditEntryHalResource.class)
    })
    public ResponseEntity<Object> findAudits(@PathVariable UUID documentId) {
        StoredDocument storedDocument =
            Optional.ofNullable(storedDocumentRepository.findOne(documentId))
                .orElseThrow(() -> new StoredDocumentNotFoundException(documentId));

        List<StoredDocumentAuditEntry> auditEntries = auditEntryService.findStoredDocumentAudits(storedDocument);

        Resources<StoredDocumentAuditEntryHalResource> resources = new Resources<>(auditEntries
            .stream()
            .map(StoredDocumentAuditEntryHalResource::new)
            .collect(Collectors.toList()));

        return ResponseEntity.ok().contentType(V1MediaType.V1_HAL_AUDIT_ENTRY_COLLECTION_MEDIA_TYPE).body(resources);
    }

}
