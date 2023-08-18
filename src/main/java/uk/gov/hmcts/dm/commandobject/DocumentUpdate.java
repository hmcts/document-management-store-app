package uk.gov.hmcts.dm.commandobject;

import lombok.Data;

import java.util.Map;
import java.util.UUID;
import javax.validation.constraints.NotBlank;

@Data
public class DocumentUpdate {
    public final @NotBlank UUID documentId;
    public final @NotBlank Map<String, String> metadata;
}
