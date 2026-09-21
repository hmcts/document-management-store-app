package uk.gov.hmcts.dm.componenttests.sugar;

import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.type.CollectionType;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomResultMatcher implements ResultMatcher {

    private final JsonMapper objectMapper;
    private final Class<Object> expectedClass;
    private final List<ResultMatcher> matchers = new ArrayList<>();

    public CustomResultMatcher(JsonMapper objectMapper) {
        this(objectMapper, null);
    }

    public CustomResultMatcher(JsonMapper objectMapper, Class<Object> expectedClass) {
        this.objectMapper = objectMapper;
        this.expectedClass = expectedClass;
    }

    public CustomResultMatcher hasPropertyEqualTo(String name, Object value) {
        matchers.add(result -> {
            Object actual = objectMapper.readValue(result.getResponse().getContentAsByteArray(), expectedClass);
            assertThat(actual).hasFieldOrPropertyWithValue(name, value);
        });
        return this;
    }

    public CustomResultMatcher isEqualTo(Object expected) {
        matchers.add(result -> {
            Object actual = objectMapper.readValue(result.getResponse().getContentAsByteArray(), expected.getClass());
            assertThat(actual).isEqualTo(expected);
        });
        return this;
    }

    public CustomResultMatcher containsExactly(Object... expected) {
        matchers.add(result -> {
            CollectionType valueType = objectMapper.getTypeFactory().constructCollectionType(List.class, expectedClass);
            List<Object> actual = objectMapper.readValue(result.getResponse().getContentAsByteArray(), valueType);
            assertThat(actual).containsExactly(expected);
        });
        return this;
    }

    @Override
    public void match(MvcResult result) throws Exception {
        for (ResultMatcher matcher : matchers) {
            matcher.match(result);
        }
    }
}
