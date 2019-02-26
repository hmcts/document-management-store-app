package uk.gov.hmcts.dm.hateos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.ResourceSupport;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
public abstract class HalResource extends ResourceSupport {

    private final Map<String, ResourceSupport> embedded = new HashMap<>();

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty("_embedded")
    public Map<String, ResourceSupport> getEmbeddedResources() {
        return embedded;
    }

    public void embedResource(String relationship, ResourceSupport resource) {
        embedded.put(relationship, resource);
    }

    @JsonIgnore
    public final URI getUri() {
        try {
            return getLink("self") != null ? new URI(getLink("self").getHref()) : null;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
