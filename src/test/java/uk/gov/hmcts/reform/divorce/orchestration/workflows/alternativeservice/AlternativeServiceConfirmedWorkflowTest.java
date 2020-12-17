package uk.gov.hmcts.reform.divorce.orchestration.workflows.alternativeservice;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.alternativeservice.ApplyForDnPetitionerEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.alternativeservice.ApplyForDnPetitionerSolicitorEmailTask;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVED_BY_ALTERNATIVE_METHOD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.mockTasksExecution;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTaskWasCalled;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTaskWasNeverCalled;

@RunWith(MockitoJUnitRunner.class)
public class AlternativeServiceConfirmedWorkflowTest {

    @Mock
    private ApplyForDnPetitionerEmailTask applyForDnPetitionerEmailTask;

    @Mock
    private ApplyForDnPetitionerSolicitorEmailTask applyForDnPetitionerSolicitorEmailTask;

    @InjectMocks
    private AlternativeServiceConfirmedWorkflow alternativeServiceConfirmedWorkflow;

    @Test
    public void whenPetitionerIsNotRepresentedAwaitingDnPetitionerEmailTaskIsExecuted() throws Exception {
        HashMap<String, Object> caseData = new HashMap<>();
        caseData.put(SERVED_BY_ALTERNATIVE_METHOD, YES_VALUE);
        mockTasksExecution(caseData, applyForDnPetitionerEmailTask);

        Map<String, Object> returned = alternativeServiceConfirmedWorkflow.run(
            CaseDetails.builder()
                .caseData(caseData)
                .build()
        );

        assertThat(returned, is(caseData));
        verifyTaskWasCalled(caseData, applyForDnPetitionerEmailTask);
        verifyTaskWasNeverCalled(applyForDnPetitionerSolicitorEmailTask);
    }

    @Test
    public void whenPetitionerIsRepresentedAwaitingDnPetitionerSolicitorEmailTaskIsExecuted() throws Exception {
        HashMap<String, Object> caseData = new HashMap<>();
        caseData.put(SERVED_BY_ALTERNATIVE_METHOD, YES_VALUE);
        caseData.put(PETITIONER_SOLICITOR_EMAIL, TEST_EMAIL);

        mockTasksExecution(caseData, applyForDnPetitionerSolicitorEmailTask);

        Map<String, Object> returned = alternativeServiceConfirmedWorkflow.run(
            CaseDetails.builder()
                .caseData(caseData)
                .build()
        );

        assertThat(returned, is(caseData));
        verifyTaskWasCalled(caseData, applyForDnPetitionerSolicitorEmailTask);
        verifyTaskWasNeverCalled(applyForDnPetitionerEmailTask);
    }

    @Test
    public void whenServedByProcessServerIsNoDontExecuteAnyTask() throws Exception {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(SERVED_BY_ALTERNATIVE_METHOD, NO_VALUE);

        runTestExpectingNoTaskExecutedForInput(caseData);
    }

    @Test
    public void whenServedByProcessServerDoesntExistDontExecuteAnyTask() throws Exception {
        runTestExpectingNoTaskExecutedForInput(new HashMap<>());
    }

    private void runTestExpectingNoTaskExecutedForInput(Map<String, Object> caseData) throws WorkflowException {
        Map<String, Object> returned = alternativeServiceConfirmedWorkflow.run(
            CaseDetails.builder()
                .caseData(caseData)
                .build()
        );

        assertThat(returned, is(caseData));
        verifyTaskWasNeverCalled(applyForDnPetitionerSolicitorEmailTask);
        verifyTaskWasNeverCalled(applyForDnPetitionerEmailTask);
    }
}
