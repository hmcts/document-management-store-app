package uk.gov.hmcts.dm.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.dm.commandobject.DocumentUpdate;
import uk.gov.hmcts.dm.commandobject.UpdateDocumentCommand;
import uk.gov.hmcts.dm.commandobject.UpdateDocumentsCommand;
import uk.gov.hmcts.dm.config.V1MediaType;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.exception.DocumentUpdateException;
import uk.gov.hmcts.dm.hateos.StoredDocumentHalResource;
import uk.gov.hmcts.dm.service.AuditedStoredDocumentOperationsService;

import java.util.UUID;

import static java.util.AbstractMap.SimpleEntry;

@RestController
@RequestMapping(
    path = "/documents")
@Api("Endpoint for Update of Documents")
@ConditionalOnProperty("toggle.ttl")
public class StoredDocumentUpdateController {

    @Autowired
    private AuditedStoredDocumentOperationsService documentService;

    @PatchMapping(value = "/{documentId}",
        consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Updates document instance (ex. ttl)")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Returns representation of the new state",  response = StoredDocumentHalResource.class)
    })
    @Transactional
    public ResponseEntity<Object> updateDocument(@PathVariable UUID documentId,
                                         @RequestBody UpdateDocumentCommand updateDocumentCommand) {

        StoredDocument storedDocument = documentService.updateDocument(documentId, updateDocumentCommand);

        return ResponseEntity
            .ok()
            .contentType(V1MediaType.V1_HAL_DOCUMENT_MEDIA_TYPE)
            .body(new StoredDocumentHalResource(storedDocument));

    }

    @PatchMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Bulk update of document TTL and metadata")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Update completed",  response = StoredDocumentHalResource.class),
        @ApiResponse(code = 400, message = "Bad request")
    })
    @Transactional
    public ResponseEntity<Object> updateDocuments(@RequestBody UpdateDocumentsCommand updateDocumentsCommand) {

        try {
            for (DocumentUpdate d : updateDocumentsCommand.documents) {
                documentService.updateDocument(d.documentId, d.metadata, updateDocumentsCommand.ttl);
            }
        } catch (Exception e) {
            throw new DocumentUpdateException(e.getMessage());
        }

        return ResponseEntity
            .ok()
            .body(new SimpleEntry("result", "Success"));
    }

}


