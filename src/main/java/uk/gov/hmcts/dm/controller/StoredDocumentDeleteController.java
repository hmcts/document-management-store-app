package uk.gov.hmcts.dm.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.dm.commandobject.DeleteCaseDocumentsCommand;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.exception.InvalidRequestException;
import uk.gov.hmcts.dm.response.CaseDocumentsDeletionResults;
import uk.gov.hmcts.dm.service.AuditedStoredDocumentOperationsService;
import uk.gov.hmcts.dm.service.SearchService;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static uk.gov.hmcts.dm.utils.InputParamsVerifier.verifyRequestParamsAreNotEmpty;
import static uk.gov.hmcts.dm.utils.InputParamsVerifier.verifyRequestParamsConditions;

@RestController
@Slf4j
@RequestMapping(
    path = "/documents/")
@Api("Endpoint for Deletion of Documents")
@ConditionalOnProperty("toggle.deleteenabled")
public class StoredDocumentDeleteController {

    @Autowired
    private AuditedStoredDocumentOperationsService auditedStoredDocumentOperationsService;

    @Autowired
    private SearchService searchService;

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

    @ApiOperation(
        value = "Marks case documents as soft deleted.",
        notes = "This operation marks documents related to a specific case as soft deleted"
    )
    @ApiResponses(value = {
        @ApiResponse(code = 200,
            message = "Case documents deletion results",
            response = CaseDocumentsDeletionResults.class),
        @ApiResponse(code = 400,
            message = "Bad Request",
            response = CaseDocumentsDeletionResults.class),
        @ApiResponse(code = 403,
            message = "Forbidden",
            response = CaseDocumentsDeletionResults.class),
        @ApiResponse(code = 500,
            message = "Internal Server Error",
            response = CaseDocumentsDeletionResults.class)
    })
    @PostMapping(
        path = "/delete",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<CaseDocumentsDeletionResults> deleteCaseDocuments(
        @Valid @RequestBody DeleteCaseDocumentsCommand deleteCaseDocumentsCommand) {

        try {
            verifyRequestParamsAreNotEmpty(deleteCaseDocumentsCommand);
            verifyRequestParamsConditions(deleteCaseDocumentsCommand);

            List<StoredDocument> storedDocuments = searchService.findStoredDocumentsByCaseRef(deleteCaseDocumentsCommand);
            CaseDocumentsDeletionResults caseDocumentsDeletionResults = auditedStoredDocumentOperationsService.deleteCaseStoredDocuments(storedDocuments);

            return new ResponseEntity<>(caseDocumentsDeletionResults, OK);

        } catch (final InvalidRequestException invalidRequestException) {
            log.error("deleteCaseDocuments API call failed due to error - {}",
                invalidRequestException.getMessage(),
                invalidRequestException
            );
            return new ResponseEntity<>(null, BAD_REQUEST);
        } catch (final Exception exception) {
            log.error("deleteCaseDocuments API call failed due to error - {}",
                exception.getMessage(),
                exception
            );
            return new ResponseEntity<>(null, INTERNAL_SERVER_ERROR);
        }
    }

}
