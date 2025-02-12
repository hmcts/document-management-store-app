package uk.gov.hmcts.dm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.dm.commandobject.DeleteCaseDocumentsCommand;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.exception.InvalidRequestException;
import uk.gov.hmcts.dm.response.CaseDocumentsDeletionResults;
import uk.gov.hmcts.dm.service.AuditedStoredDocumentOperationsService;
import uk.gov.hmcts.dm.service.SearchService;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.dm.utils.InputParamsVerifier.verifyRequestParamsAreNotEmpty;
import static uk.gov.hmcts.dm.utils.InputParamsVerifier.verifyRequestParamsConditions;

@RestController
@Slf4j
@RequestMapping(
    path = "/documents/")
@Tag(name = "StoredDocumentDelete Service", description = "Endpoint for Deletion of Documents")
public class StoredDocumentDeleteController {

    private final AuditedStoredDocumentOperationsService auditedStoredDocumentOperationsService;

    private final SearchService searchService;

    @Autowired
    public StoredDocumentDeleteController(AuditedStoredDocumentOperationsService auditedStoredDocumentOperationsService,
                                          SearchService searchService) {
        this.auditedStoredDocumentOperationsService = auditedStoredDocumentOperationsService;
        this.searchService = searchService;
    }

    @DeleteMapping(value = "{documentId}")
    @Operation(summary = "Deletes a Stored Document.",
        parameters = {
            @Parameter(in = ParameterIn.HEADER, name = "serviceauthorization",
                description = "Service Authorization (S2S Bearer token)", required = true,
                schema = @Schema(type = "string")),
            @Parameter(in = ParameterIn.HEADER, name = "user-id", description = "User Id",
                schema = @Schema(type = "string"))
        }
    )
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

    @Operation(
        summary = "Marks case documents as soft deleted.",
        parameters = {
            @Parameter(in = ParameterIn.HEADER, name = "serviceauthorization",
                description = "Service Authorization (S2S Bearer token)", required = true,
                schema = @Schema(type = "string"))
        }
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
            description = "Case documents deletion results"),
        @ApiResponse(responseCode = "400",
            description = "Bad Request"),
        @ApiResponse(responseCode = "403",
            description = "Forbidden")
    })
    @PostMapping(
        path = "/delete",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<CaseDocumentsDeletionResults> deleteCaseDocuments(
        @Valid @RequestBody DeleteCaseDocumentsCommand deleteCaseDocumentsCommand) {

        try {
            verifyRequestParamsAreNotEmpty(deleteCaseDocumentsCommand);
            verifyRequestParamsConditions(deleteCaseDocumentsCommand);

            List<StoredDocument> storedDocuments =
                searchService.findStoredDocumentsIdsByCaseRef(deleteCaseDocumentsCommand);
            CaseDocumentsDeletionResults caseDocumentsDeletionResults =
                auditedStoredDocumentOperationsService.deleteCaseStoredDocuments(storedDocuments);

            return new ResponseEntity<>(caseDocumentsDeletionResults, OK);

        } catch (final InvalidRequestException invalidRequestException) {
            log.error("deleteCaseDocuments API call failed due to error - {}",
                invalidRequestException.getMessage(),
                invalidRequestException
            );
            return new ResponseEntity<>(null, BAD_REQUEST);
        }
    }

}
