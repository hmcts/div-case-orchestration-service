package uk.gov.hmcts.reform.divorce.orchestration.util;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.hmcts.reform.divorce.orchestration.exception.InvalidPropertyException;

import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.rules.ExpectedException.none;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.util.JsonPayloadUtils.getBooleanFromPayloadField;

public class JsonPayloadUtilsTest {

    private static final String TEST_KEY = "booleanField";

    @Rule
    public ExpectedException expectedException = none();

    @Test
    public void testBooleanResponsesFromPayloadMapAreCorrect() throws InvalidPropertyException {
        HashMap<String, Object> payload = new HashMap<>();
        payload.put(TEST_KEY, YES_VALUE);
        assertThat(getBooleanFromPayloadField(payload, TEST_KEY), is(true));

        payload.put(TEST_KEY, NO_VALUE);
        assertThat(getBooleanFromPayloadField(payload, TEST_KEY), is(false));
    }

    @Test
    public void testExceptionIsThrown_WhenValueIsNotPresent() throws InvalidPropertyException {
        expectedException.expectMessage("Could not evaluate value of property \"" + TEST_KEY + "\"");
        expectedException.expect(InvalidPropertyException.class);

        HashMap<String, Object> payload = new HashMap<>();
        getBooleanFromPayloadField(payload, TEST_KEY);
    }

    @Test
    public void testExceptionIsThrown_WhenValueIsNotValid() throws InvalidPropertyException {
        expectedException.expectMessage("Could not evaluate value of property \"" + TEST_KEY + "\"");
        expectedException.expect(InvalidPropertyException.class);

        HashMap<String, Object> payload = new HashMap<>();
        payload.put(TEST_KEY, "what?");
        getBooleanFromPayloadField(payload, TEST_KEY);
    }

}