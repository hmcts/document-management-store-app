package uk.gov.hmcts.dm.componenttests.sugar;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomResultMatcher implements ResultMatcher {

    private final ObjectMapper objectMapper;
    private final Class<Object> expectedClass;
    private final List<ResultMatcher> matchers = new ArrayList<>();

    public CustomResultMatcher(ObjectMapper objectMapper) {
        this(objectMapper, null);
    }

    public CustomResultMatcher(ObjectMapper objectMapper, Class<Object> expectedClass) {
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

    // TODO - removed because "Possible heap pollution from parameterized vararg type O" EM-186
    // public <I, O> CustomResultMatcher containsExactly(Function<I, O> mapper, O... expected) {
    //     matchers.add(result -> {
    //         CollectionType valueType =
    //          objectMapper.getTypeFactory().constructCollectionType(List.class, expectedClass);
    //         List<I> actual = objectMapper.readValue(result.getResponse().getContentAsByteArray(), valueType);
    //         assertThat(actual.stream().map(mapper).collect(toList())).containsExactly(expected);
    //     });
    //     return this;
    // }

    @Override
    public void match(MvcResult result) throws Exception {
        for (ResultMatcher matcher : matchers) {
            matcher.match(result);
        }
    }
}
