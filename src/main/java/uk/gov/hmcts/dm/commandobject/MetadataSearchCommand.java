package uk.gov.hmcts.dm.commandobject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetadataSearchCommand {

    @NotNull
    private String name;

    @NotNull
    private String value;

}
