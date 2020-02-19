package uk.gov.hmcts.dm.commandobject;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Data
public class UpdateDocumentsCommand {
    public static final String UPDATE_SUCCESS = "Success";
    public final Date ttl;
    public final @NotNull @NotEmpty List<DocumentUpdate> documents;

}
