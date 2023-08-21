package uk.gov.hmcts.dm.commandobject;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteCaseDocumentsCommand {

    @NotNull
    private String caseRef;
}
