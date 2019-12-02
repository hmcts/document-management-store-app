package uk.gov.hmcts.dm.functional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Toggles {

    @Value("${toggle.metadatasearchendpoint}")
    private String metadatasearchendpoint;
    @Value("${toggle.documentandmetadatauploadendpoint}")
    private String documentandmetadatauploadendpoint;
    @Value("${toggle.folderendpoint}")
    private String folderendpoint;
    @Value("${toggle.includeidamhealth}")
    private String includeidamhealth;
    @Value("${toggle.ttl}")
    private String ttl;

    public boolean isMetadatasearchendpoint() {
        return Boolean.valueOf(metadatasearchendpoint);
    }

    public boolean isDocumentandmetadatauploadendpoint() {
        return Boolean.valueOf(documentandmetadatauploadendpoint);
    }

    public boolean isFolderendpoint() {
        return Boolean.valueOf(folderendpoint);
    }

    public boolean isIncludeidamhealth() {
        return Boolean.valueOf(includeidamhealth);
    }

    public boolean isTtl() {
        return Boolean.valueOf(ttl);
    }
}
