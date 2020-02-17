package uk.gov.hmcts.dm.commandobject;

import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
public class DocumentUpdate {
    public final UUID documentId;
    public final Map<String, String> metadata;
}
