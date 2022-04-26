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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.dm.config.V1MediaType;
import uk.gov.hmcts.dm.domain.Folder;
import uk.gov.hmcts.dm.hateos.FolderHalResource;
import uk.gov.hmcts.dm.service.Constants;
import uk.gov.hmcts.dm.service.FolderService;
import uk.gov.hmcts.dm.service.StoredDocumentService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(
    path = "/folders",
    produces = V1MediaType.V1_FOLDER_MEDIA_TYPE_VALUE)
@Tag(name = "Folder Service", description = "Endpoint for Folder management")
@ConditionalOnProperty("toggle.folderendpoint")
public class FolderController {

    @InitBinder
    public void initBinder(WebDataBinder binder) {

        binder.setDisallowedFields(Constants.IS_ADMIN);
    }

    @Autowired
    private FolderService folderService;

    @Autowired
    private StoredDocumentService storedDocumentService;

    @PostMapping("")
    @Operation(summary = "Create a Folder.",
        parameters = {
            @Parameter(in = ParameterIn.HEADER, name = "serviceauthorization",
                description = "Service Authorization (S2S Bearer token)", required = true,
                schema = @Schema(type = "string"))})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "403", description = "Access Denied")
    })
    public ResponseEntity<FolderHalResource> post(@RequestBody FolderHalResource folderHalResource) {
        Folder folder = new Folder();
        folderService.save(folder);
        return ResponseEntity.ok(new FolderHalResource(folder));
    }

    @PostMapping(value = "/{id}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Adds a list of Stored Documents to a Folder (Stored Documents are created from "
        + "uploaded Documents)",
        parameters = {
            @Parameter(in = ParameterIn.HEADER, name = "serviceauthorization",
                description = "Service Authorization (S2S Bearer token)", required = true,
                schema = @Schema(type = "string"))})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "404", description = "Folder not found"),
        @ApiResponse(responseCode = "403", description = "Access Denied")
    })
    public ResponseEntity<Object> addDocuments(@PathVariable UUID id, @RequestParam List<MultipartFile> files) {

        return folderService.findById(id)
            .map(folder -> {
                storedDocumentService.saveItemsToBucket(folder, files);
                return ResponseEntity.noContent().build();
            })
            .orElse(ResponseEntity.notFound().build());

    }

    @GetMapping("{id}")
    @Operation(summary = "Retrieves JSON representation of a Folder.",
        parameters = {
            @Parameter(in = ParameterIn.HEADER, name = "serviceauthorization",
                description = "Service Authorization (S2S Bearer token)", required = true,
                schema = @Schema(type = "string"))})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "404", description = "Folder not found"),
        @ApiResponse(responseCode = "403", description = "Access Denied")
    })
    public ResponseEntity<?> get(@PathVariable UUID id) {
        return folderService
            .findById(id)
            .map(FolderHalResource::new)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("{id}")
    @Operation(summary = "Deletes a Folder.",
        parameters = {
            @Parameter(in = ParameterIn.HEADER, name = "serviceauthorization",
                description = "Service Authorization (S2S Bearer token)", required = true,
                schema = @Schema(type = "string"))})
    public ResponseEntity<Object> delete(@PathVariable UUID id) {
        return ResponseEntity.status(405).build();
    }

}
