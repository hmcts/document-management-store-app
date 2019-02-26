package uk.gov.hmcts.dm.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;

import java.util.UUID;

@JsonPropertyOrder({DocumentContentVersionModel.ATTRIBUTE_DOCUMENT_ID, DocumentContentVersionModel
    .ATTRIBUTE_VERSION_ID, DocumentContentVersionModel.ATTRIBUTE_URI, DocumentContentVersionModel.ATTRIBUTE_CHECKSUM})
@NoArgsConstructor
class DocumentContentVersionModel {

    static final String ATTRIBUTE_DOCUMENT_ID = "document_id";
    static final String ATTRIBUTE_VERSION_ID = "version_id";
    static final String ATTRIBUTE_URI = "uri";
    static final String ATTRIBUTE_CHECKSUM = "checksum";

    @JsonProperty(ATTRIBUTE_DOCUMENT_ID)
    @Getter
    private UUID documentId;

    @JsonProperty(ATTRIBUTE_VERSION_ID)
    @Getter
    private UUID versionId;

    @JsonProperty(ATTRIBUTE_URI)
    @Getter
    private String uri;

    @JsonProperty(ATTRIBUTE_CHECKSUM)
    @Getter
    private String checksum;

    DocumentContentVersionModel(DocumentContentVersion dcv) {
        this.documentId = dcv.getStoredDocument().getId();
        this.versionId = dcv.getId();
        this.uri = dcv.getContentUri();
        this.checksum = dcv.getContentChecksum();
    }
}
