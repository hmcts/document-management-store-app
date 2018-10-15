package uk.gov.hmcts.dm.commandobject;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class FolderSingleSearchCommand {
    @NotNull
    private String q;

}
