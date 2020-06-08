package uk.gov.hmcts.dm.hateos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@EqualsAndHashCode(callSuper = true)
public abstract class HalResource extends RepresentationModel {

    private final Map<String, RepresentationModel> embedded = new HashMap<>();

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty("_embedded")
    public Map<String, RepresentationModel> getEmbeddedResources() {
        return embedded;
    }

    public void embedResource(String relationship, RepresentationModel resource) {
        embedded.put(relationship, resource);
    }

    @JsonIgnore
    public final URI getUri() {
        Optional<Link> link = getLink("self");

        return link.map(Link::toUri).orElse(null);
    }
}
