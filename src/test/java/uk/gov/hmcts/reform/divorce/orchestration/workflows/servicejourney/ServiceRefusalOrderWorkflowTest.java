package uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BasePayloadSpecificDocumentGenerationTaskTest;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.ServiceRefusalOrderTask;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.ServiceRefusalOrderTask.DRAFT_DECISION;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.ServiceRefusalOrderTask.FINAL_DECISION;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTaskWasCalled;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksWereNeverCalled;

@RunWith(MockitoJUnitRunner.class)
public class ServiceRefusalOrderWorkflowTest extends BasePayloadSpecificDocumentGenerationTaskTest {

    @Mock
    private ServiceRefusalOrderTask serviceRefusalOrderTask;

    @InjectMocks
    private ServiceRefusalOrderWorkflow classUnderTest;

    @Test
    public void whenServiceDecisionMadeWorkflowThenExpectedTaskRuns() throws WorkflowException {
        CaseDetails caseDetails =  buildCaseDetails(CcdStates.AWAITING_SERVICE_CONSIDERATION);

        when(serviceRefusalOrderTask.execute(any(), anyMap())).thenReturn(caseDetails.getCaseData());

        Map<String, Object> returnedData = classUnderTest.run(caseDetails, FINAL_DECISION, AUTH_TOKEN);

        assertThat(returnedData, is(notNullValue()));
        verifyTaskWasCalled(returnedData, serviceRefusalOrderTask);
    }

    @Test
    public void whenCaseIsNotAwaitingServiceConsiderationTaskShouldNotGenerateDocument() throws WorkflowException {
        CaseDetails caseDetails = buildCaseDetails(CcdStates.AWAITING_CLARIFICATION);

        Map<String, Object> returnedData = classUnderTest.run(caseDetails, FINAL_DECISION, AUTH_TOKEN);

        assertThat(returnedData, is(notNullValue()));
        verifyTasksWereNeverCalled(serviceRefusalOrderTask);
    }

    @Test
    public void whenCaseIsNotAwaitingServiceConsiderationTaskShouldNotGenerateDraft() throws WorkflowException {
        CaseDetails caseDetails = buildCaseDetails(CcdStates.AWAITING_CLARIFICATION);

        Map<String, Object> returnedData = classUnderTest.run(caseDetails, DRAFT_DECISION, AUTH_TOKEN);

        assertThat(returnedData, is(notNullValue()));
        verifyTasksWereNeverCalled(serviceRefusalOrderTask);
    }

    private CaseDetails buildCaseDetails(String caseState) {
        Map<String, Object> caseData = ImmutableMap.of("anyKey", "anyValue");
        return CaseDetails.builder()
            .caseData(caseData)
            .caseId("1234")
            .state(caseState)
            .build();
    }

}