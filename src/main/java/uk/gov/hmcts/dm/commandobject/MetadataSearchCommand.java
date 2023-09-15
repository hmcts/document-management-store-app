package uk.gov.hmcts.dm.commandobject;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetadataSearchCommand {

    @NotNull
    private String name;

    @NotNull
    private String value;

}
