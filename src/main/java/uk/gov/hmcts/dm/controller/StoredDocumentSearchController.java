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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.dm.commandobject.MetadataSearchCommand;
import uk.gov.hmcts.dm.config.V1MediaType;
import uk.gov.hmcts.dm.hateos.StoredDocumentHalResource;
import uk.gov.hmcts.dm.service.Constants;
import uk.gov.hmcts.dm.service.SearchService;
import uk.gov.hmcts.dm.service.SecurityUtilService;

import javax.validation.Valid;

@RestController
@RequestMapping(
        path = "/documents")
@Tag(name = "StoredDocumentSearch Service", description = "Endpoint for Searching Documents")
@ConditionalOnProperty("toggle.metadatasearchendpoint")
public class StoredDocumentSearchController {

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields(Constants.IS_ADMIN);
    }

    @Autowired
    private SearchService searchService;

    @Autowired
    private SecurityUtilService securityUtilService;

    @PostMapping(value = "/filter", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Search stored documents using metadata.",
        parameters = {
            @Parameter(in = ParameterIn.HEADER, name = "serviceauthorization",
                description = "Service Authorization (S2S Bearer token)", required = true,
                schema = @Schema(type = "string"))})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Returns search results"),
        @ApiResponse(responseCode = "400", description = "Error when search criteria not specified"),
        @ApiResponse(responseCode = "403", description = "Access Denied")
    })
    public ResponseEntity<Object> search(
        @Valid @RequestBody MetadataSearchCommand metadataSearchCommand,
        Pageable pageable,
        PagedResourcesAssembler<StoredDocumentHalResource> assembler) {

        Page<StoredDocumentHalResource> page = searchService.findStoredDocumentsByMetadata(metadataSearchCommand, pageable).map(StoredDocumentHalResource::new);

        return ResponseEntity
                .ok()
                .contentType(V1MediaType.V1_HAL_DOCUMENT_PAGE_MEDIA_TYPE)
                .body(assembler.toModel(page));
    }


    @PostMapping(value = "/owned")
    @Operation(summary = "Search stored documents by ownership.",
        parameters = {
            @Parameter(in = ParameterIn.HEADER, name = "serviceauthorization",
                description = "Service Authorization (S2S Bearer token)", required = true,
                schema = @Schema(type = "string"))})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Returns search results"),
        @ApiResponse(responseCode = "400", description = "Error when search criteria not specified"),
        @ApiResponse(responseCode = "403", description = "Access Denied")
    })
    public ResponseEntity<Object> searchOwned(
            Pageable pageable,
            PagedResourcesAssembler<StoredDocumentHalResource> assembler) {

        Page<StoredDocumentHalResource> page = searchService.findStoredDocumentsByCreator(securityUtilService.getUserId(), pageable).map(StoredDocumentHalResource::new);

        return ResponseEntity
                .ok()
                .contentType(V1MediaType.V1_HAL_DOCUMENT_PAGE_MEDIA_TYPE)
                .body(assembler.toModel(page));
    }

}
