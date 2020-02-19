package uk.gov.hmcts.dm.commandobject;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;

@Data
public class DocumentUpdate {
    public final @NotNull UUID documentId;
    public final @NotNull Map<String, String> metadata;
}
