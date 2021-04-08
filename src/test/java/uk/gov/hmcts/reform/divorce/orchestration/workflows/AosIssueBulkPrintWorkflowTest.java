package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AosPackDueDateSetterTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FetchPrintDocsFromDmStoreTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ServiceMethodValidationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateNoticeOfProceedingsDetailsTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.CoRespondentAosPackPrinterTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.RespondentAosPackPrinterTask;
import uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.RESPONDENT_SOLICITOR_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_SOL_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.CaseDataTestHelper.buildOrganisationPolicy;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.mockTasksExecution;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksCalledInOrder;

@RunWith(MockitoJUnitRunner.class)
public class AosIssueBulkPrintWorkflowTest {

    @Mock
    private ServiceMethodValidationTask serviceMethodValidationTask;

    @Mock
    private FetchPrintDocsFromDmStoreTask fetchPrintDocsFromDmStoreTask;

    @Mock
    private RespondentAosPackPrinterTask respondentAosPackPrinterTask;

    @Mock
    private CoRespondentAosPackPrinterTask coRespondentAosPackPrinterTask;

    @Mock
    private AosPackDueDateSetterTask aosPackDueDateSetterTask;

    @Mock
    private UpdateNoticeOfProceedingsDetailsTask updateRespondentDigitalDetailsTask;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private CaseDataUtils caseDataUtils;

    @InjectMocks
    private AosIssueBulkPrintWorkflow classUnderTest;

    private Map<String, Object> payload;

    private CaseDetails caseDetails;

    @Before
    public void setUp() {
        payload = new HashMap<>();
        payload.put(RESP_SOL_REPRESENTED, YES_VALUE);

        caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .caseData(payload)
            .build();

        when(featureToggleService.isFeatureEnabled(Features.REPRESENTED_RESPONDENT_JOURNEY)).thenReturn(true);
        when(caseDataUtils.isAdulteryCaseWithNamedCoRespondent(payload)).thenReturn(true);

        mockTaskExecution();
    }

    @Test
    public void whenWorkflowRuns_WithDigitalRespSol_AndNamedCoRespondent_allTasksRun_payloadReturned() throws WorkflowException, TaskException {
        payload.put(RESPONDENT_SOLICITOR_ORGANISATION_POLICY, buildOrganisationPolicy());

        Map<String, Object> response = classUnderTest.run(AUTH_TOKEN, caseDetails);
        assertThat(response, is(payload));

        verifyTasksCalledInOrder(
            payload,
            serviceMethodValidationTask,
            fetchPrintDocsFromDmStoreTask,
            respondentAosPackPrinterTask,
            coRespondentAosPackPrinterTask,
            aosPackDueDateSetterTask,
            updateRespondentDigitalDetailsTask
        );
    }

    @Test
    public void whenWorkflowRunsForNonAdulteryCase_allTasksRunExceptForUpdateNoticeDetails_ToggledOff() throws WorkflowException, TaskException {
        payload.put(RESPONDENT_SOLICITOR_ORGANISATION_POLICY, buildOrganisationPolicy());
        when(featureToggleService.isFeatureEnabled(Features.REPRESENTED_RESPONDENT_JOURNEY)).thenReturn(false);

        Map<String, Object> response = classUnderTest.run(AUTH_TOKEN, caseDetails);
        assertThat(response, is(payload));

        verifyTasksCalledInOrder(
            payload,
            serviceMethodValidationTask,
            fetchPrintDocsFromDmStoreTask,
            respondentAosPackPrinterTask,
            coRespondentAosPackPrinterTask,
            aosPackDueDateSetterTask
        );

        verifyNoInteractions(updateRespondentDigitalDetailsTask);
    }

    @Test
    public void whenWorkflowRunsForNonAdulteryCase_WithDigitalRespSol_allTasksButCoRespondentRun() throws WorkflowException, TaskException {
        when(caseDataUtils.isAdulteryCaseWithNamedCoRespondent(payload)).thenReturn(false);
        payload.put(RESPONDENT_SOLICITOR_ORGANISATION_POLICY, buildOrganisationPolicy());

        Map<String, Object> response = classUnderTest.run(AUTH_TOKEN, caseDetails);
        assertThat(response, is(payload));

        verifyTasksCalledInOrder(
            payload,
            serviceMethodValidationTask,
            fetchPrintDocsFromDmStoreTask,
            respondentAosPackPrinterTask,
            aosPackDueDateSetterTask,
            updateRespondentDigitalDetailsTask
        );

        verifyNoInteractions(coRespondentAosPackPrinterTask);
    }

    @Test
    public void whenRespIsRepresented_AndRespSolIsNotDigital_someTasksRun() throws WorkflowException, TaskException {
        Map<String, Object> response = classUnderTest.run(AUTH_TOKEN, caseDetails);
        assertThat(response, is(payload));

        verifyTasksCalledInOrder(
            payload,
            serviceMethodValidationTask,
            fetchPrintDocsFromDmStoreTask,
            coRespondentAosPackPrinterTask,
            aosPackDueDateSetterTask
        );

        verifyNoInteractions(respondentAosPackPrinterTask);
        verifyNoInteractions(updateRespondentDigitalDetailsTask);
    }

    @Test
    public void whenToggleIsOff_RespIsRepresented_AndRespSolIsNotDigital_someTasksRun() throws WorkflowException, TaskException {
        when(featureToggleService.isFeatureEnabled(Features.REPRESENTED_RESPONDENT_JOURNEY)).thenReturn(false);

        Map<String, Object> response = classUnderTest.run(AUTH_TOKEN, caseDetails);
        assertThat(response, is(payload));

        verifyTasksCalledInOrder(
            payload,
            serviceMethodValidationTask,
            fetchPrintDocsFromDmStoreTask,
            respondentAosPackPrinterTask,
            coRespondentAosPackPrinterTask,
            aosPackDueDateSetterTask
        );

        verifyNoInteractions(updateRespondentDigitalDetailsTask);
    }

    @Test
    public void whenRespIsNotRepresented_RespSolIsNotDigital_allTasksRunExceptForUpdateNoticeDetails() throws WorkflowException, TaskException {
        Map<String, Object> caseData = caseDetails.getCaseData();
        caseData.put(RESP_SOL_REPRESENTED, NO_VALUE);
        caseDetails.setCaseData(caseData);

        Map<String, Object> response = classUnderTest.run(AUTH_TOKEN, caseDetails);
        assertThat(response, is(payload));

        verifyTasksCalledInOrder(
            payload,
            serviceMethodValidationTask,
            fetchPrintDocsFromDmStoreTask,
            respondentAosPackPrinterTask,
            coRespondentAosPackPrinterTask,
            aosPackDueDateSetterTask
        );

        verifyNoInteractions(updateRespondentDigitalDetailsTask);
    }

    private void mockTaskExecution() {
        mockTasksExecution(
            payload,
            serviceMethodValidationTask,
            fetchPrintDocsFromDmStoreTask,
            aosPackDueDateSetterTask,
            respondentAosPackPrinterTask,
            coRespondentAosPackPrinterTask,
            updateRespondentDigitalDetailsTask
        );
    }

}