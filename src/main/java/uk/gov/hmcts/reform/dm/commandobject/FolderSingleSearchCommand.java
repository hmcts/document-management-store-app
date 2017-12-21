package uk.gov.hmcts.reform.dm.commandobject;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Created by pawel on 14/06/2017.
 */
@Data
public class FolderSingleSearchCommand {
    @NotNull
    private String q;

}
