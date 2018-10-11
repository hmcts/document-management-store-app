package uk.gov.hmcts.dm.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.dm.service.AuditedStoredDocumentOperationsService;

import java.util.UUID;

@RestController
@RequestMapping(
    path = "/documents/")
@Api("Endpoint for Deletion of Documents")
@ConditionalOnProperty("toggle.deleteenabled")
public class StoredDocumentDeleteController {

    @Autowired
    private AuditedStoredDocumentOperationsService auditedStoredDocumentOperationsService;

    @DeleteMapping(value = "{documentId}")
    @ApiOperation("Deletes a Stored Document.")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "No Content returned"),
    })
    public ResponseEntity<Object> deleteDocument(@PathVariable
                                                        UUID documentId,
                                                    @RequestParam(
                                                        value = "permanent",
                                                        required = false,
                                                        defaultValue = "false")
                                                        boolean permanent) {

        auditedStoredDocumentOperationsService.deleteStoredDocument(documentId, permanent);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}


