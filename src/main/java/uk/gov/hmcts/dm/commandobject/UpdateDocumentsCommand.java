package uk.gov.hmcts.dm.commandobject;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class UpdateDocumentsCommand {
    public final Date ttl;
    public final @NotEmpty List<DocumentUpdate> documents;
}
