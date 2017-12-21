package uk.gov.hmcts.reform.dm.commandobject;

import lombok.Getter;
import lombok.Setter;
import java.util.Map;

/**
 * Created by pawel on 04/10/2017.
 */
public class UploadDocumentsAndMetadataCommand extends UploadDocumentsCommand {

    @Getter
    @Setter
    private Map<String, String> metadata;

}
