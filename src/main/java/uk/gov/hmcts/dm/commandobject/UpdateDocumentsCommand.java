package uk.gov.hmcts.dm.commandobject;

import lombok.Data;

import java.util.Date;
import java.util.List;
import javax.validation.constraints.NotEmpty;

@Data
public class UpdateDocumentsCommand {
    public final Date ttl;
    public final @NotEmpty List<DocumentUpdate> documents;
}
