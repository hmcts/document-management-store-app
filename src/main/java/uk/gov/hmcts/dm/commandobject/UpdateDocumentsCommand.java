package uk.gov.hmcts.dm.commandobject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class UpdateDocumentsCommand {
    public final Date ttl;
    public final @NotEmpty List<DocumentUpdate> documents;

    @JsonCreator
    public UpdateDocumentsCommand(
        @JsonProperty("ttl") Date ttl,
        @JsonProperty("documents") List<DocumentUpdate> documents
    ) {
        this.ttl = ttl;
        this.documents = documents;
    }
}
