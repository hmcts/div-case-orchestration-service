package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateDivorceCaseHearingDetailsWithinBulk;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class BulkCaseUpdateHearingDetailsEventWorkflowTest {

    @Spy
    private ObjectMapper objectMapper;

    @Mock
    private UpdateDivorceCaseHearingDetailsWithinBulk updateDivorceCaseHearingDetailsWithinBulk;

    @InjectMocks
    private BulkCaseUpdateHearingDetailsEventWorkflow classUnderTest;

    @Test
    public void whenProcessAwaitingPronouncement_thenProcessAsExpected() throws WorkflowException {
        final ImmutablePair<String, Object> authTokenPair = new ImmutablePair<>(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);

        final Task[] tasks = new Task[]{
            updateDivorceCaseHearingDetailsWithinBulk,
        };

        Map<String, Object> expected = Collections.emptyMap();

        when(classUnderTest.execute(tasks, null, authTokenPair)).thenReturn(expected);

        Map<String, Object> actual = classUnderTest.run(CcdCallbackRequest.builder().build(), AUTH_TOKEN);

        assertEquals(expected, actual);
    }
}
