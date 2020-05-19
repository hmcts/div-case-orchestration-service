package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.WelshContinueTask;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class WelshContinueWorkflowTest {

    @Mock
    private WelshContinueTask welshContinueTask;

    private WelshContinueWorkflow spyWelshContinueWorkflow;

    @Before
    public void beforeTest() {
        WelshContinueWorkflow welshContinueWorkflow = new WelshContinueWorkflow(welshContinueTask);
        spyWelshContinueWorkflow = Mockito.spy(welshContinueWorkflow);
    }

    @Test
    public void testRunMethod() throws WorkflowException {

        String authToken = "AUTH_TOKEN";
        String caseId = "999";
        Map<String, Object> caseData = Collections.EMPTY_MAP;
        CaseDetails caseDetails = CaseDetails.builder().caseId(caseId).caseData(caseData).build();
        CcdCallbackRequest callbackRequest = new CcdCallbackRequest();
        callbackRequest.setCaseDetails(caseDetails);
        spyWelshContinueWorkflow.run(callbackRequest, authToken);
        ArgumentCaptor<Task[]> taskCaptor = ArgumentCaptor.forClass(Task[].class);
        ArgumentCaptor<Pair> authTokenPair = ArgumentCaptor.forClass(Pair.class);
        ArgumentCaptor<Pair> caseIdPair = ArgumentCaptor.forClass(Pair.class);
        verify(spyWelshContinueWorkflow).execute(taskCaptor.capture(), same(caseData), authTokenPair.capture(), caseIdPair.capture());
        Task[] tasks = {welshContinueTask};
        assertThat(taskCaptor.getValue()).contains(tasks);
        assertThat(authTokenPair.getValue().getValue()).isEqualTo(authToken);
        assertThat(caseIdPair.getValue().getValue()).isEqualTo(caseId);
    }

}
