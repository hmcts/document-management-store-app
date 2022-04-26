package uk.gov.hmcts.dm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.dm.service.AuditedStoredDocumentOperationsService;

import java.util.UUID;

@RestController
@RequestMapping(
    path = "/documents/")
@Tag(name = "StoredDocumentDelete Service", description = "Endpoint for Deletion of Documents")
@ConditionalOnProperty("toggle.deleteenabled")
public class StoredDocumentDeleteController {

    @Autowired
    private AuditedStoredDocumentOperationsService auditedStoredDocumentOperationsService;


    @DeleteMapping(value = "{documentId}")
    @Operation(summary = "Deletes a Stored Document.",
        parameters = {
            @Parameter(in = ParameterIn.HEADER, name = "serviceauthorization",
                description = "Service Authorization (S2S Bearer token)", required = true,
                schema = @Schema(type = "string"))})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "No Content returned"),
        @ApiResponse(responseCode = "403", description = "Access Denied")
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


