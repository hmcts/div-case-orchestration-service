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
import uk.gov.hmcts.reform.divorce.orchestration.tasks.WelshContinueInterceptTask;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class WelshContinueInterceptWorkflowTest {

    @Mock
    private WelshContinueInterceptTask welshContinueInterceptTask;

    private WelshContinueInterceptWorkflow spyWelshContinueInterceptWorkflow;

    @Before
    public void beforeTest() {
        WelshContinueInterceptWorkflow welshContinueInterceptWorkflow = new WelshContinueInterceptWorkflow(welshContinueInterceptTask);
        spyWelshContinueInterceptWorkflow = Mockito.spy(welshContinueInterceptWorkflow);
    }

    @Test
    public void testRunMethod() throws WorkflowException {

        String authToken = "AUTH_TOKEN";
        String caseId = "999";
        Map<String, Object> caseData = Collections.EMPTY_MAP;
        CaseDetails caseDetails = CaseDetails.builder().caseId(caseId).caseData(caseData).build();
        CcdCallbackRequest callbackRequest = new CcdCallbackRequest();
        callbackRequest.setCaseDetails(caseDetails);
        spyWelshContinueInterceptWorkflow.run(callbackRequest, authToken);
        ArgumentCaptor<Task[]> taskCaptor = ArgumentCaptor.forClass(Task[].class);
        ArgumentCaptor<Pair> authTokenPair = ArgumentCaptor.forClass(Pair.class);
        ArgumentCaptor<Pair> caseIdPair = ArgumentCaptor.forClass(Pair.class);
        ArgumentCaptor<Pair> caseDetailPair = ArgumentCaptor.forClass(Pair.class);
        verify(spyWelshContinueInterceptWorkflow).execute(taskCaptor.capture(), same(caseData),
            caseDetailPair.capture(), authTokenPair.capture(), caseIdPair.capture());
        Task[] tasks = {welshContinueInterceptTask};
        assertThat(caseDetailPair.getValue().getValue()).isEqualTo(caseDetails);
    }

}
