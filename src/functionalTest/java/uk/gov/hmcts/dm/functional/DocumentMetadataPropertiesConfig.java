package uk.gov.hmcts.dm.functional;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "large-docs")
@Component
public class DocumentMetadataPropertiesConfig {

    private Map<String, DocumentMetadata> metadata = new HashMap<>();

    public Map<String, DocumentMetadata> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, DocumentMetadata> metadata) {
        this.metadata = metadata;
    }

    protected static class DocumentMetadata {
        private String documentName;
        private String extension;
        private long size;

        public String getDocumentName() {
            return documentName;
        }

        public void setDocumentName(String documentName) {
            this.documentName = documentName;
        }

        public String getExtension() {
            return extension;
        }

        public void setExtension(String extension) {
            this.extension = extension;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }
    }

}
