package uk.gov.hmcts.dm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    @PostConstruct
    void init() throws NoSuchMethodException {
        uploadDocumentsCommandMethodParameter = new MethodParameter(
                StoredDocumentController.class.getMethod(
                        "createFrom",
                        UploadDocumentsCommand.class,
                        BindingResult.class), 0);
    }

    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Creates a list of Stored Documents by uploading a list of binary/text files.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success")
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
    @Operation(summary = "Retrieves JSON representation of a Stored Document.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success")
    })
    public ResponseEntity<Object> getMetaData(@PathVariable UUID documentId) {

        StoredDocument storedDocument = auditedStoredDocumentOperationsService.readStoredDocument(documentId);

        if (storedDocument == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity
                .ok()
                .contentType(V1MediaType.V1_HAL_DOCUMENT_MEDIA_TYPE)
                .body(new StoredDocumentHalResource(storedDocument));
    }

    @GetMapping(value = "{documentId}/binary")
    @Operation(summary = "Streams contents of the most recent Document Content Version associated with the Stored Document.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Returns contents of a file")
    })
    public ResponseEntity<Void> getBinary(@PathVariable UUID documentId, HttpServletResponse response,
                                          @RequestHeader Map<String, String> headers,
                                          HttpServletRequest httpServletRequest) {
        var documentContentVersion =
            documentContentVersionService.findMostRecentDocumentContentVersionByStoredDocumentId(
                    documentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        response.setHeader(HttpHeaders.CONTENT_TYPE, documentContentVersion.getMimeType());
        response.setHeader(HttpHeaders.CONTENT_LENGTH, documentContentVersion.getSize().toString());
        response.setHeader("OriginalFileName", documentContentVersion.getOriginalDocumentName());
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
            format("fileName=\"%s\"", documentContentVersion.getOriginalDocumentName()));

        try {
            response.setHeader("data-source", "contentURI");
            auditedDocumentContentVersionOperationsService.readDocumentContentVersionBinaryFromBlobStore(
                documentContentVersion,
                response.getOutputStream());
        } catch (UncheckedIOException | IOException e) {
            logger.warn("IOException streaming response", e);

            logger.info(String.format("Headers for documentId : %s starts", documentId));
            logger.info(String.format("ContentType for documentId : %s is : %s ", documentId,
                    documentContentVersion.getMimeType()));
            logger.info(String.format("Size for documentId : %s is : %s ", documentId,
                    documentContentVersion.getSize()));
            headers.forEach((key, value) ->
                logger.info(String.format("documentId : %s has Request Header %s = %s",
                    documentId.toString(), key, value)));
            logger.info(String.format("Headers for documentId : %s ends", documentId));
        }
        return ResponseEntity.ok().build();
    }
}

