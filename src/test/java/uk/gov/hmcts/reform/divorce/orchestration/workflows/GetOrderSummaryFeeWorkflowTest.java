package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GetGeneralApplicationWithoutNoticeFeeTask;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
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
        HashMap<String, Object> caseData = new HashMap<>();

        mockTasksExecution(
            caseData,
            getGeneralApplicationWithoutNoticeFeeTask

        );

        Map<String, Object> returned = getOrderSummaryFeeWorkflow.run(
            CcdCallbackRequest.builder()
                .caseDetails(CaseDetails.builder()
                    .caseData(caseData)
                    .build())
                .build()
        );

        MatcherAssert.assertThat(returned, is(caseData));

        verifyTaskWasCalled(
            caseData,
            getGeneralApplicationWithoutNoticeFeeTask
        );
    }

}