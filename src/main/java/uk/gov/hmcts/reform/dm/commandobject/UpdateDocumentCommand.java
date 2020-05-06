package uk.gov.hmcts.reform.dm.commandobject;

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
