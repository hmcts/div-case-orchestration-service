package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GetCaseWithIdMapFlow;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetCourtHearingDetailsFromBulkCase;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateCaseInCCD;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;

@RunWith(MockitoJUnitRunner.class)
public class UpdateCourtHearingDetailsWorkflowTest {

    @Mock
    private GetCaseWithIdMapFlow getCaseWithIdMapFlow;

    @Mock
    private SetCourtHearingDetailsFromBulkCase setCourtHearingDetailsFromBulkCase;

    @Mock
    private UpdateCaseInCCD updateCaseInCCD;

    @InjectMocks
    private UpdateCourtHearingDetailsWorkflow classUnderTest;

    @Test
    public void setCourtHearingDetailsFromBulkCase_thenProceedAsExpected() throws TaskException, WorkflowException {
        Map<String, Object> expected = Collections.emptyMap();

        Map<String, Object> actual = classUnderTest.run(Collections.emptyMap(), TEST_CASE_ID, AUTH_TOKEN);

        assertEquals(expected, actual);
    }
}
