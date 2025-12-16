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

@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("java:S1452")
public abstract class HalResource<T extends HalResource<T>>
    extends RepresentationModel<T> {

    private final Map<String, RepresentationModel<?>> embedded = new HashMap<>();

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty("_embedded")
    public Map<String, RepresentationModel<?>> getEmbeddedResources() {
        return embedded;
    }

    public void embedResource(String relation, RepresentationModel<?> resource) {
        embedded.put(relation, resource);
    }

    @JsonIgnore
    public final URI getUri() {
        return getLink("self").map(Link::toUri).orElse(null);
    }
}
