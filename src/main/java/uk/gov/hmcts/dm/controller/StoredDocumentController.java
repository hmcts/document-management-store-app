package uk.gov.hmcts.dm.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
import org.springframework.web.bind.annotation.*;
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
import java.util.*;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;

@SuppressWarnings({"squid:S2629", "squid:S1452"})
@RestController
@RequestMapping(path = "/documents")
@Api("Endpoint for Stored Document Management")
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
    @ApiOperation("Creates a list of Stored Documents by uploading a list of binary/text files.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success", response = StoredDocumentHalResourceCollection.class)
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
    @ApiOperation("Retrieves JSON representation of a Stored Document.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success", response = StoredDocumentHalResource.class)
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
    @ApiOperation("Streams contents of the most recent Document Content Version associated with the Stored Document.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Returns contents of a file")
    })
    public ResponseEntity<?> getBinary(@PathVariable UUID documentId, HttpServletResponse response,
                                       @RequestHeader Map<String, String> headers,
                                       HttpServletRequest httpServletRequest) {
        return documentContentVersionService.findMostRecentDocumentContentVersionByStoredDocumentId(documentId)
            .map(documentContentVersion -> {

                response.setHeader(HttpHeaders.CONTENT_TYPE, documentContentVersion.getMimeType());
                response.setHeader(HttpHeaders.CONTENT_LENGTH, documentContentVersion.getSize().toString());
                response.setHeader("OriginalFileName", documentContentVersion.getOriginalDocumentName());
                response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    format("fileName=\"%s\"", documentContentVersion.getOriginalDocumentName()));

                try {
                    if (isBlank(documentContentVersion.getContentUri())) {
                        response.setHeader("data-source", "Postgres");
                        auditedDocumentContentVersionOperationsService.readDocumentContentVersionBinary(
                            documentContentVersion,
                            response.getOutputStream());
                    } else {
                        response.setHeader("data-source", "contentURI");
                        auditedDocumentContentVersionOperationsService.readDocumentContentVersionBinaryFromBlobStore(
                            documentContentVersion,
                            response.getOutputStream());
                    }

                } catch (IOException e) {
                    return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(e);
                } catch (Exception e) {
                    if (Objects.nonNull(headers)) {
                        logger.info(String.format("Headers for documentId : %s starts", documentId.toString()));
                        logger.info(String.format("ContentType for documentId : %s is : %s ", documentId.toString(),
                            documentContentVersion.getMimeType()));
                        logger.info(String.format("Size for documentId : %s is : %s ", documentId.toString(),
                            documentContentVersion.getSize().toString()));
                        headers.forEach((key, value) ->
                            logger.info(String.format("documentId : %s has Request Header %s = %s",
                                documentId.toString(), key, value)));
                        logger.info(String.format("Headers for documentId : %s ends", documentId.toString()));
                    } else {
                        logger.info(String.format("Header is null for documentId : %s ", documentId.toString()));
                        if (Objects.nonNull(httpServletRequest)) {
                            Iterator<String> stringIterator = httpServletRequest.getHeaderNames().asIterator();
                            while (stringIterator.hasNext()) {
                                logger.info(String.format("HeaderNames for documentId : %s  is %s ",
                                    documentId.toString(), stringIterator.next()));
                            }
                        }
                    }
                    return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(e);
                }

                return ResponseEntity.ok().build();

            })
            .orElse(ResponseEntity.notFound().build());
    }
}

