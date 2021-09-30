package uk.gov.hmcts.reform.divorce.orchestration.workflows.generalemail;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.generalemail.ClearGeneralEmailFieldsTask;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.context;

@RunWith(MockitoJUnitRunner.class)
public class ClearGeneralEmailFieldsWorkflowTest {

    private final Map<String, Object> expectedOutput = ImmutableMap.of("any key", "any value");

    @Mock
    private ClearGeneralEmailFieldsTask clearGeneralEmailFieldsTask;

    @InjectMocks
    private ClearGeneralEmailFieldsWorkflow clearGeneralEmailFieldsWorkflow;

    @Test
    public void whenTheWorkflowRuns_thenExpectedTaskIsExecuted() throws WorkflowException {
        when(clearGeneralEmailFieldsTask.execute(any(), any())).thenReturn(expectedOutput);

        Map<String, Object> result = clearGeneralEmailFieldsWorkflow.run(CaseDetails.builder().caseId(TEST_CASE_ID).caseData(emptyMap()).build());

        assertThat(result, is(expectedOutput));
        verify(clearGeneralEmailFieldsTask).execute(context(), emptyMap());
    }
}
