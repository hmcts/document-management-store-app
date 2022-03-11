package uk.gov.hmcts.dm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.dm.config.V1MediaType;
import uk.gov.hmcts.dm.exception.StoredDocumentNotFoundException;
import uk.gov.hmcts.dm.hateos.StoredDocumentAuditEntryHalResource;
import uk.gov.hmcts.dm.repository.StoredDocumentRepository;
import uk.gov.hmcts.dm.service.AuditEntryService;

import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/documents")
@Tag(name = "StoredDocumentAudit Service", description = "Endpoint for Audit entities")
public class StoredDocumentAuditController {

    @Autowired
    private AuditEntryService auditEntryService;

    @Autowired
    private StoredDocumentRepository storedDocumentRepository;

    @GetMapping("{documentId}/auditEntries")
    @Operation(summary = "Retrieves audits related to a Stored Document.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success")
    })
    public ResponseEntity<CollectionModel<StoredDocumentAuditEntryHalResource>> findAudits(@PathVariable UUID documentId) {
        return storedDocumentRepository
            .findById(documentId)
            .map(storedDocument -> ResponseEntity.ok()
                .contentType(V1MediaType.V1_HAL_AUDIT_ENTRY_COLLECTION_MEDIA_TYPE).body(CollectionModel.of(new ArrayList<StoredDocumentAuditEntryHalResource>(
                    auditEntryService
                        .findStoredDocumentAudits(storedDocument)
                        .stream()
                        .map(StoredDocumentAuditEntryHalResource::new)
                        .collect(Collectors.toList())))))
            .orElseThrow(() -> new StoredDocumentNotFoundException(documentId));
    }

}
