package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.TestConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GetGeneralApplicationWithoutNoticeFeeTask;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.mockTasksExecution;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTaskWasCalled;

@RunWith(MockitoJUnitRunner.class)
public class GetOrderSummaryFeeWorkflowTest {

    @Mock
    private GetGeneralApplicationWithoutNoticeFeeTask getGeneralApplicationWithoutNoticeFeeTask;


    @InjectMocks
    private GetOrderSummaryFeeWorkflow getOrderSummaryFeeWorkflow;

    @Test
    public void whenGeneralApplicationWithoutNoticeFee_thenProcessAsExpected() throws Exception {
        prepareTaskContext();

        HashMap<String, Object> caseData = new HashMap<>();

        mockTasksExecution(
            caseData,
            getGeneralApplicationWithoutNoticeFeeTask

        );

        executeWorkflowRun(caseData);

        verifyTaskWasCalled(
            caseData,
            getGeneralApplicationWithoutNoticeFeeTask
        );
    }

    private void executeWorkflowRun(HashMap<String, Object> caseData) throws WorkflowException {
        Map<String, Object> returned = getOrderSummaryFeeWorkflow.run(
            CcdCallbackRequest.builder()
                .caseDetails(CaseDetails.builder()
                    .caseData(caseData)
                    .build())
                .build()
        );

        MatcherAssert.assertThat(returned, is(caseData));
    }

    public static TaskContext prepareTaskContext() {
        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, TestConstants.TEST_CASE_ID);
        context.setTransientObject(CASE_DETAILS_JSON_KEY, CaseDetails.builder().build());
        return context;
    }

}