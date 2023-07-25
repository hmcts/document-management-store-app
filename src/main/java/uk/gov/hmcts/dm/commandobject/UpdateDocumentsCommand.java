package uk.gov.hmcts.dm.commandobject;

import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import java.util.Date;
import java.util.List;

@Data
public class UpdateDocumentsCommand {
    public final Date ttl;
    public final @NotEmpty List<DocumentUpdate> documents;
}
