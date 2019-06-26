package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetDnGrantedDetailsFromBulkCase;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateCaseInCCD;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.BULK_CASE_DETAILS_CONTEXT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.UPDATE_BULK_DN_PRONOUNCEMENT_DETAILS_EVENT;

@RunWith(MockitoJUnitRunner.class)
public class UpdatePronouncementDateWorkflowTest {

    @Mock
    private SetDnGrantedDetailsFromBulkCase setDnGrantedDetailsFromBulkCase;

    @Mock
    private UpdateCaseInCCD updateCaseInCCD;

    @InjectMocks
    private UpdatePronouncementDateWorkflow classUnderTest;

    @Test
    public void setDecreeNisiPronouncementDateFromBulkCase_thenProceedAsExpected() throws TaskException, WorkflowException {
        final Task[] tasks = new Task[] {
            setDnGrantedDetailsFromBulkCase,
            updateCaseInCCD
        };

        Map<String, Object> expected = Collections.singletonMap("Hello", "World");

        when(classUnderTest.execute(
            tasks,
            new HashMap<>(),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN),
            ImmutablePair.of(BULK_CASE_DETAILS_CONTEXT_KEY, Collections.emptyMap()),
            ImmutablePair.of(CASE_EVENT_ID_JSON_KEY, UPDATE_BULK_DN_PRONOUNCEMENT_DETAILS_EVENT),
            ImmutablePair.of(CASE_ID_JSON_KEY, TEST_CASE_ID)
        )).thenReturn(expected);

        Map<String, Object> actual = classUnderTest.run(Collections.emptyMap(), TEST_CASE_ID, AUTH_TOKEN);

        assertEquals(expected, actual);
    }
}
