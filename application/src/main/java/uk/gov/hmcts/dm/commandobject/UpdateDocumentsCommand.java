package uk.gov.hmcts.dm.commandobject;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class UpdateDocumentsCommand {
    public static final String UPDATE_SUCCESS = "Success";
    public final Date ttl;
    public final List<DocumentUpdate> documents;

}
