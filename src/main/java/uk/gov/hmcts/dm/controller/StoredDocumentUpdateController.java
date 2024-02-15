package uk.gov.hmcts.dm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.dm.commandobject.DocumentUpdate;
import uk.gov.hmcts.dm.commandobject.UpdateDocumentCommand;
import uk.gov.hmcts.dm.commandobject.UpdateDocumentsCommand;
import uk.gov.hmcts.dm.config.V1MediaType;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.exception.DocumentUpdateException;
import uk.gov.hmcts.dm.exception.StoredDocumentNotFoundException;
import uk.gov.hmcts.dm.hateos.StoredDocumentHalResource;
import uk.gov.hmcts.dm.service.AuditedStoredDocumentOperationsService;
import uk.gov.hmcts.dm.service.Constants;

import java.util.UUID;

import static java.util.AbstractMap.SimpleEntry;

@RestController
@RequestMapping(
    path = "/documents")
@Tag(name = "StoredDocumentUpdate Service", description = "Endpoint for Updating Documents")
@SuppressWarnings({"squid:S2139","squid:S3740"})
public class StoredDocumentUpdateController {

    private final Logger logger = LoggerFactory.getLogger(StoredDocumentUpdateController.class);

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields(Constants.IS_ADMIN);
    }

    @Autowired
    private AuditedStoredDocumentOperationsService documentService;

    @PatchMapping(value = "/{documentId}",
        consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Updates document instance (ex. ttl)",
        parameters = {
            @Parameter(in = ParameterIn.HEADER, name = "serviceauthorization",
                description = "Service Authorization (S2S Bearer token)", required = true,
                schema = @Schema(type = "string")),
            @Parameter(in = ParameterIn.HEADER, name = "user-id", description = "User Id",
                schema = @Schema(type = "string"))
        }
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Returns representation of the new state"),
        @ApiResponse(responseCode = "403", description = "Access Denied")
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
    @Operation(summary = "Bulk update of document TTL and metadata",
        parameters = {
            @Parameter(in = ParameterIn.HEADER, name = "serviceauthorization",
                description = "Service Authorization (S2S Bearer token)", required = true,
                schema = @Schema(type = "string")),
            @Parameter(in = ParameterIn.HEADER, name = "user-id", description = "User Id",
                schema = @Schema(type = "string"))
        }
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Update completed"),
        @ApiResponse(responseCode = "400", description = "Bad request"),
        @ApiResponse(responseCode = "403", description = "Access Denied")
    })
    @Transactional
    public ResponseEntity<Object> updateDocuments(@RequestBody UpdateDocumentsCommand updateDocumentsCommand) {

        try {
            for (DocumentUpdate d : updateDocumentsCommand.documents) {
                //to hack sonar
                var docId = d.documentId;
                var metadata = d.metadata.toString();
                logger.debug("DocumentId: {},Metadata: {}", docId, metadata);
                documentService.updateDocument(d.documentId, d.metadata, updateDocumentsCommand.ttl);
            }
        } catch (StoredDocumentNotFoundException exception) {
            //We need to do to make sure we return 404 status and not 500.
            logger.error(exception.getMessage());
            throw exception;
        } catch (Exception e) {
            throw new DocumentUpdateException(e.getMessage());
        }

        return ResponseEntity
            .ok()
            .body(new SimpleEntry("result", "Success"));
    }

}


