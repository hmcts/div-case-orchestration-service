package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.JUDGE_COSTS_DECISION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@RunWith(MockitoJUnitRunner.class)
public class AddJudgeCostsDecisionToPayloadTaskTest {

    @InjectMocks
    private AddJudgeCostsDecisionToPayloadTask classUnderTest;

    @Test
    public void shouldReturnYesInPayload() throws TaskException {
        Map<String, Object> returnedPayload = classUnderTest.execute(null, singletonMap("incomingKey", "incomingValue"));

        assertThat(returnedPayload, hasEntry("incomingKey", "incomingValue"));
        assertThat(returnedPayload, hasEntry(JUDGE_COSTS_DECISION, YES_VALUE));
    }
}