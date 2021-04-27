package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddJudgeCostsDecisionToPayloadTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.JudgeCostsClaimFieldsRemovalTask;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.JUDGE_COSTS_CLAIM_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.mockTasksExecution;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksCalledInOrder;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksWereNeverCalled;

@RunWith(MockitoJUnitRunner.class)
public class JudgeCostsDecisionWorkflowTest {

    @InjectMocks
    private JudgeCostsDecisionWorkflow classUnderTest;

    @Mock
    private JudgeCostsClaimFieldsRemovalTask judgeCostsClaimFieldsRemovalTask;

    @Mock
    private AddJudgeCostsDecisionToPayloadTask addJudgeCostsDecisionToPayloadTask;

    @Test
    public void shouldRunCorrectTask_whenJudgeCostsClaimGranted() throws WorkflowException {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(JUDGE_COSTS_CLAIM_GRANTED, YES_VALUE);
        CaseDetails caseDetails = buildCaseDetails(caseData);

        mockTasksExecution(
            caseData,
            addJudgeCostsDecisionToPayloadTask
        );

        Map<String, Object> returnedData = executeWorkflow(caseDetails);

        verifyTasksCalledInOrder(
            returnedData,
            addJudgeCostsDecisionToPayloadTask
        );

        verifyTasksWereNeverCalled(judgeCostsClaimFieldsRemovalTask);

    }

    @Test
    public void shouldRunCorrectTask_whenJudgeCostsClaimNotGranted() throws WorkflowException {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(JUDGE_COSTS_CLAIM_GRANTED, NO_VALUE);
        CaseDetails caseDetails = buildCaseDetails(caseData);

        mockTasksExecution(
                caseData,
                addJudgeCostsDecisionToPayloadTask,
                judgeCostsClaimFieldsRemovalTask
        );

        Map<String, Object> returnedData = executeWorkflow(caseDetails);

        verifyTasksCalledInOrder(
                returnedData,
                addJudgeCostsDecisionToPayloadTask,
                judgeCostsClaimFieldsRemovalTask
        );
    }

    @Test
    public void shouldRunCorrectTask_whenJudgeCostsClaimIsAdjourn() throws WorkflowException {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(JUDGE_COSTS_CLAIM_GRANTED, "Adjourn");
        CaseDetails caseDetails = buildCaseDetails(caseData);

        mockTasksExecution(
                caseData,
                addJudgeCostsDecisionToPayloadTask,
                judgeCostsClaimFieldsRemovalTask
        );

        Map<String, Object> returnedData = executeWorkflow(caseDetails);

        verifyTasksCalledInOrder(
                returnedData,
                addJudgeCostsDecisionToPayloadTask,
                judgeCostsClaimFieldsRemovalTask
        );
    }

    private CaseDetails buildCaseDetails(Map<String, Object> caseData) {
        return CaseDetails.builder()
            .caseData(caseData)
            .caseId(TEST_CASE_ID)
            .build();
    }

    private Map<String, Object> executeWorkflow(CaseDetails caseDetails)
        throws WorkflowException {
        Map<String, Object> returnedData = classUnderTest.run(caseDetails);
        assertThat(returnedData, is(caseDetails.getCaseData()));

        return returnedData;
    }
}
