package uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import uk.gov.hmcts.reform.divorce.model.response.ValidationResponse;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class ValidationResponseUTest {
    private final List<String> errors;
    private final List<String> warning;
    private final boolean result;

    public ValidationResponseUTest(List<String> errors, List<String> warning, boolean result) {
        this.errors = errors;
        this.warning = warning;
        this.result = result;
    }

    @Parameterized.Parameters
    public static Collection data() {
        return Arrays.asList(new Object[][] {
            { null, null, true },
            { null, Collections.emptyList(), true },
            { Collections.emptyList(), null, true },
            { Collections.emptyList(), Collections.emptyList(), true },
            { Collections.singletonList("something"), null, false },
            { Collections.singletonList("something"), Collections.emptyList(), false },
            { null, Collections.singletonList("something"), false },
            { Collections.emptyList(), Collections.singletonList("something"), false },
            { Collections.singletonList("something"), Collections.singletonList("something"), false }
        });
    }

    @Test
    public void whenIsValid_returnExpectedResponse() {
        ValidationResponse validationResponse =
            ValidationResponse.builder()
            .warnings(warning)
            .errors(errors)
            .build();

        assertEquals(result, validationResponse.isValid());
    }
}