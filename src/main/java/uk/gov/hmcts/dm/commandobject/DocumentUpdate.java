package uk.gov.hmcts.dm.commandobject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
public class DocumentUpdate {
    public final @NotBlank UUID documentId;
    public final @NotBlank Map<String, String> metadata;

    @JsonCreator
    public DocumentUpdate(
        @JsonProperty("documentId") UUID documentId,
        @JsonProperty("metadata") Map<String, String> metadata
    ) {
        this.documentId = documentId;
        this.metadata = metadata;
    }
}
