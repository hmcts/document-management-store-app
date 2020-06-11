package uk.gov.hmcts.dm.commandobject;

import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
public class UpdateDocumentCommand {

    private Date ttl;

    private Map<String, String> metadata;

}
