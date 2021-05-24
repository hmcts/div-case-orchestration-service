package uk.gov.hmcts.reform.divorce.orchestration.workflows.generalemail;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.generalemail.StoreGeneralEmailFieldsTask;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class StoreGeneralEmailFieldsWorkflowTest {

    private final Map<String, Object> expectedOutput = ImmutableMap.of("any key", "any value");

    @Mock
    private StoreGeneralEmailFieldsTask storeGeneralEmailFieldsTask;

    @InjectMocks
    private StoreGeneralEmailFieldsWorkflow storeGeneralEmailFieldsWorkflow;

    @Test
    public void whenTheWorkflowRuns_thenExpectedTaskIsExecuted() throws WorkflowException {
        when(storeGeneralEmailFieldsTask.execute(any(), any())).thenReturn(expectedOutput);

        Map<String, Object> result = storeGeneralEmailFieldsWorkflow.run(CaseDetails.builder().caseId(TEST_CASE_ID).caseData(emptyMap()).build(), AUTH_TOKEN);

        assertThat(result, is(expectedOutput));

        TaskContext expectedTaskContext = new DefaultTaskContext();
        expectedTaskContext.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        verify(storeGeneralEmailFieldsTask).execute(expectedTaskContext, emptyMap());
    }
}
