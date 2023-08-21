package uk.gov.hmcts.dm.commandobject;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import java.util.UUID;

@Data
public class DocumentUpdate {
    public final @NotBlank UUID documentId;
    public final @NotBlank Map<String, String> metadata;
}
