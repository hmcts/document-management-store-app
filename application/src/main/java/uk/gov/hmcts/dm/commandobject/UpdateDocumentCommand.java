package uk.gov.hmcts.dm.commandobject;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Data
public class UpdateDocumentCommand {

    @Getter
    @Setter
    private Date ttl;

}
