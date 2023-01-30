package uk.gov.hmcts.dm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.catalina.connector.ClientAbortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.dm.commandobject.UploadDocumentsCommand;
import uk.gov.hmcts.dm.config.ToggleConfiguration;
import uk.gov.hmcts.dm.config.V1MediaType;
import uk.gov.hmcts.dm.domain.StoredDocument;
import uk.gov.hmcts.dm.hateos.StoredDocumentHalResource;
import uk.gov.hmcts.dm.hateos.StoredDocumentHalResourceCollection;
import uk.gov.hmcts.dm.service.AuditedDocumentContentVersionOperationsService;
import uk.gov.hmcts.dm.service.AuditedStoredDocumentOperationsService;
import uk.gov.hmcts.dm.service.Constants;
import uk.gov.hmcts.dm.service.DocumentContentVersionService;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.lang.String.format;

@SuppressWarnings({"squid:S2629", "squid:S1452"})
@RestController
@RequestMapping(path = "/documents")
@Tag(name = "StoredDocument Service", description = "Endpoint for Stored Document Management")
public class StoredDocumentController {

    private final Logger logger = LoggerFactory.getLogger(StoredDocumentController.class);

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields(Constants.IS_ADMIN);
    }

    @Autowired
    private DocumentContentVersionService documentContentVersionService;

    @Autowired
    private AuditedStoredDocumentOperationsService auditedStoredDocumentOperationsService;

    @Autowired
    private AuditedDocumentContentVersionOperationsService auditedDocumentContentVersionOperationsService;

    private MethodParameter uploadDocumentsCommandMethodParameter;

    @Autowired
    private ToggleConfiguration toggleConfiguration;

    @PostConstruct
    void init() throws NoSuchMethodException {
        uploadDocumentsCommandMethodParameter = new MethodParameter(
                StoredDocumentController.class.getMethod(
                        "createFrom",
                        UploadDocumentsCommand.class,
                        BindingResult.class), 0);
    }

    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Creates a list of Stored Documents by uploading a list of binary/text files.",
        parameters = {
            @Parameter(in = ParameterIn.HEADER, name = "serviceauthorization",
                description = "Service Authorization (S2S Bearer token)", required = true,
                schema = @Schema(type = "string"))})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success",
            content = @Content(schema = @Schema(implementation = StoredDocumentHalResourceCollection.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "405", description = "Validation exception"),
        @ApiResponse(responseCode = "403", description = "Access Denied")
    })
    public ResponseEntity<Object> createFrom(
            @Valid UploadDocumentsCommand uploadDocumentsCommand,
            BindingResult result) throws MethodArgumentNotValidException {

        if (result.hasErrors()) {
            throw new MethodArgumentNotValidException(uploadDocumentsCommandMethodParameter, result);
        } else {
            List<StoredDocument> storedDocuments =
                    auditedStoredDocumentOperationsService.createStoredDocuments(uploadDocumentsCommand);
            return ResponseEntity
                    .ok()
                    .contentType(V1MediaType.V1_HAL_DOCUMENT_COLLECTION_MEDIA_TYPE)
                    .body(StoredDocumentHalResourceCollection.of(storedDocuments));
        }
    }

    @GetMapping(value = "{documentId}")
    @Operation(summary = "Retrieves JSON representation of a Stored Document.",
        parameters = {
            @Parameter(in = ParameterIn.HEADER, name = "serviceauthorization",
                description = "Service Authorization (S2S Bearer token)", required = true,
                schema = @Schema(type = "string")),
            @Parameter(in = ParameterIn.HEADER, name = "user-id", description = "User Id",
                schema = @Schema(type = "string")),
            @Parameter(in = ParameterIn.HEADER, name = "user-roles", description = "User Roles", required = true,
                schema = @Schema(type = "string"))
        }
    )

    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "404", description = "Document not found"),
        @ApiResponse(responseCode = "403", description = "Access Denied")
    })
    public ResponseEntity<Object> getMetaData(@PathVariable UUID documentId,
                                              @RequestHeader Map<String, String> headers) {

        headers.forEach((key, value) ->
            logger.debug(String.format("DocId : %s has Request Header %s = %s",
                documentId.toString(), key, value)));
        StoredDocument storedDocument = auditedStoredDocumentOperationsService.readStoredDocument(documentId);

        if (storedDocument == null) {
            return ResponseEntity.notFound().build();
        }
        logger.debug("DocumentId : {} has Response: {}", storedDocument.getId(), storedDocument);
        return ResponseEntity
                .ok()
                .contentType(V1MediaType.V1_HAL_DOCUMENT_MEDIA_TYPE)
                .body(new StoredDocumentHalResource(storedDocument));
    }

    @GetMapping(value = "{documentId}/binary")
    @Operation(summary = "Streams contents of the most recent Document Content Version associated with"
        + "the Stored Document.",
        parameters = {
            @Parameter(in = ParameterIn.HEADER, name = "serviceauthorization",
                description = "Service Authorization (S2S Bearer token)", required = true,
                schema = @Schema(type = "string")),
            @Parameter(in = ParameterIn.HEADER, name = "user-id", description = "User Id", required = true,
                schema = @Schema(type = "string")),
            @Parameter(in = ParameterIn.HEADER, name = "user-roles", description = "User Roles", required = true,
                schema = @Schema(type = "string")),
            @Parameter(in = ParameterIn.HEADER, name = "classification", description = "Classification", required = true,
                schema = @Schema(type = "string"))})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Returns contents of a file"),
        @ApiResponse(responseCode = "404", description = "Document not found"),
        @ApiResponse(responseCode = "403", description = "Access Denied")
    })
    public ResponseEntity<Void> getBinary(@PathVariable UUID documentId, HttpServletResponse response,
                                          @RequestHeader Map<String, String> headers,
                                          HttpServletRequest httpServletRequest) {
        var documentContentVersion =
            documentContentVersionService.findMostRecentDocumentContentVersionByStoredDocumentId(
                    documentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        String  xAzureRef = headers.getOrDefault("x-azure-ref", "null");
        logger.info("getBinary documentId {}, x-azure-ref: {}", documentId, xAzureRef);

        try {
            response.setHeader(HttpHeaders.CONTENT_TYPE, documentContentVersion.getMimeType());
            // Set Default content size for whole document
            response.setHeader(HttpHeaders.CONTENT_LENGTH, documentContentVersion.getSize().toString());
            response.setHeader("OriginalFileName", documentContentVersion.getOriginalDocumentName());
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                format("fileName=\"%s\"", documentContentVersion.getOriginalDocumentName()));
            response.setHeader("data-source", "contentURI");
            if (toggleConfiguration.isChunking()) {
                response.setHeader("Accept-Ranges", "bytes");
                response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, HttpHeaders.ACCEPT_RANGES);
            }
            auditedDocumentContentVersionOperationsService.readDocumentContentVersionBinaryFromBlobStore(
                documentContentVersion,
                httpServletRequest,
                response);
            logger.info("Completed getBinary documentId {}, x-azure-ref: {}", documentId, xAzureRef);
        } catch (UncheckedIOException | IOException e) {
            logger.info("Exception getBinary documentId {}, x-azure-ref: {}", documentId, xAzureRef);
            if (toggleConfiguration.isChunking()) {
                response.reset();
            }
            if (e instanceof UncheckedIOException uncheckedIoException
                && uncheckedIoException.getCause() instanceof ClientAbortException) {
                logger.warn(
                    "documentId {},IOException streaming error, broken pipe, peer closed connection {}",
                    documentId,
                    e.getMessage()
                );
            } else {
                logger.warn("IOException streaming error, for  {} ", documentId, e);
            }

            logger.debug("ContentType for documentId : {} is : {} ", documentId, documentContentVersion.getMimeType());
            logger.debug("Size for documentId : {} is : {} ", documentId, documentContentVersion.getSize());
            headers.forEach((key, value) ->
                logger.debug("documentId : {} has Request Header {} = {}", documentId.toString(), key, value));
        }
        if (toggleConfiguration.isChunking()) {
            logger.info("DocumentId : {} has Response: Content-Length, {}", documentId,
                response.getHeader(HttpHeaders.CONTENT_LENGTH));
            logger.info("DocumentId : {} has Response: Content-Type, {}", documentId,
                response.getHeader(HttpHeaders.CONTENT_TYPE));
            logger.info("DocumentId : {} has Response: Content-Range, {}", documentId,
                response.getHeader(HttpHeaders.CONTENT_RANGE));
            logger.info("DocumentId : {} has Response: Accept-Ranges, {}", documentId,
                response.getHeader(HttpHeaders.ACCEPT_RANGES));
        }
        return ResponseEntity.ok().build();
    }
}

