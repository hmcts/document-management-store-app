package uk.gov.hmcts.dm.commandobject;

import java.util.Date;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class UpdateDocumentCommand {

    @Getter
    @Setter
    private Date ttl;

}
