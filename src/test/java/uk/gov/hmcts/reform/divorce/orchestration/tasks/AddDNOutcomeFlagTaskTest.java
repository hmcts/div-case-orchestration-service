package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;

import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.core.AllOf.allOf;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_OUTCOME_FLAG_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

public class AddDNOutcomeFlagTaskTest {

    private final AddDnOutcomeFlagFieldTask classToTest = new AddDnOutcomeFlagFieldTask();

    @Test
    public void shouldAddDNOutcomeFlagPayload() {


        Map<String, Object> returnedPayload = classToTest.execute(null, singletonMap("inputTestKey", "inputTestValue"));

        assertThat(returnedPayload, allOf(
            hasEntry("inputTestKey", "inputTestValue"),
            hasEntry(DN_OUTCOME_FLAG_CCD_FIELD, YES_VALUE)
        ));
    }

}