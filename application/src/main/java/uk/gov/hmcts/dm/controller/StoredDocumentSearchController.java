package uk.gov.hmcts.dm.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.dm.commandobject.MetadataSearchCommand;
import uk.gov.hmcts.dm.config.V1MediaType;
import uk.gov.hmcts.dm.hateos.StoredDocumentHalResource;
import uk.gov.hmcts.dm.hateos.StoredDocumentHalResourceCollection;
import uk.gov.hmcts.dm.service.SearchService;
import uk.gov.hmcts.dm.service.SecurityUtilService;

import javax.validation.Valid;

@RestController
@RequestMapping(
        path = "/documents")
@Api("Search Documents")
@ConditionalOnProperty("toggle.metadatasearchendpoint")
public class StoredDocumentSearchController {

    @Autowired
    private SearchService searchService;

    @Autowired
    private SecurityUtilService securityUtilService;

    @PostMapping(value = "/filter", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Search stored documents using metadata.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Returns search results", response = StoredDocumentHalResourceCollection.class),
        @ApiResponse(code = 400, message = "Error when search criteria not specified")
    })
    public ResponseEntity<Object> search(
        @Valid @RequestBody MetadataSearchCommand metadataSearchCommand,
        Pageable pageable,
        PagedResourcesAssembler<StoredDocumentHalResource> assembler) {

        Page<StoredDocumentHalResource> page = searchService.findStoredDocumentsByMetadata(metadataSearchCommand, pageable).map(StoredDocumentHalResource::new);

        return ResponseEntity
                .ok()
                .contentType(V1MediaType.V1_HAL_DOCUMENT_PAGE_MEDIA_TYPE)
                .body(assembler.toResource(page));
    }


    @PostMapping(value = "/owned")
    @ApiOperation("Search stored documents by ownership.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns search results", response = StoredDocumentHalResourceCollection.class),
            @ApiResponse(code = 400, message = "Error when search criteria not specified")
    })
    public ResponseEntity<Object> searchOwned(
            Pageable pageable,
            PagedResourcesAssembler<StoredDocumentHalResource> assembler) {

        Page<StoredDocumentHalResource> page = searchService.findStoredDocumentsByCreator(securityUtilService.getUserId(), pageable).map(StoredDocumentHalResource::new);

        return ResponseEntity
                .ok()
                .contentType(V1MediaType.V1_HAL_DOCUMENT_PAGE_MEDIA_TYPE)
                .body(assembler.toResource(page));
    }

}
