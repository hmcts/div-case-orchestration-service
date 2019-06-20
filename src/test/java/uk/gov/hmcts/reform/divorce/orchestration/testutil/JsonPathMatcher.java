package uk.gov.hmcts.reform.divorce.orchestration.testutil;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.hamcrest.Matcher;
import org.mockito.ArgumentMatcher;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonPathMatcher {

    public static ArgumentMatcher<String> jsonPathExisteMatcher(String path) {
        return new JsonPathExistMatcher(path);
    }

    public static ArgumentMatcher<String> jsonPathValueMatcher(String path, Matcher matcher) {
        return new JsonPathValueMatcher(path, matcher);
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static class JsonPathExistMatcher implements ArgumentMatcher {
        private final String path;

        @Override
        public boolean matches(Object argument) {
            return hasJsonPath(path).matches(argument);
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static class JsonPathValueMatcher implements ArgumentMatcher {
        private final String path;
        private final Matcher matcher;

        @Override
        public boolean matches(Object argument) {
            return  hasJsonPath(path, matcher).matches(argument);
        }
    }
}