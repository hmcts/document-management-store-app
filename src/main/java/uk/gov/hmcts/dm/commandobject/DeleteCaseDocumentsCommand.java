package uk.gov.hmcts.dm.commandobject;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteCaseDocumentsCommand {

    @NotNull
    private String caseRef;
}
