package uk.gov.hmcts.dm.commandobject;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
public class DocumentUpdate {
    public final @NotBlank UUID documentId;
    public final @NotBlank Map<String, String> metadata;
}
