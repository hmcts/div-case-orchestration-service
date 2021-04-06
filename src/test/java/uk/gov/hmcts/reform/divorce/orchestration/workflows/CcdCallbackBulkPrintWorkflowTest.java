package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
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
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdEvents.ISSUE_AOS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.RESPONDENT_SOLICITOR_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_STATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.CaseDataTestHelper.buildOrganisationPolicy;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.mockTasksExecution;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksCalledInOrder;

@RunWith(MockitoJUnitRunner.class)
public class CcdCallbackBulkPrintWorkflowTest {

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
    private CcdCallbackBulkPrintWorkflow ccdCallbackBulkPrintWorkflow;

    private CcdCallbackRequest ccdCallbackRequestRequest;

    private Map<String, Object> payload;

    private TaskContext context;

    @Before
    public void setUp() {
        payload = new HashMap<>();
        payload.put(RESPONDENT_SOLICITOR_ORGANISATION_POLICY, buildOrganisationPolicy());

        CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .caseData(payload)
            .build();

        ccdCallbackRequestRequest =
            CcdCallbackRequest.builder()
                .eventId(ISSUE_AOS)
                .token(TEST_TOKEN)
                .caseDetails(caseDetails)
                .build();

        context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(CASE_DETAILS_JSON_KEY, ccdCallbackRequestRequest.getCaseDetails());
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
        context.setTransientObject(CASE_STATE_JSON_KEY, TEST_STATE);

        when(featureToggleService.isFeatureEnabled(Features.REPRESENTED_RESPONDENT_JOURNEY)).thenReturn(true);

        mockTaskExecution();
    }

    @Test
    public void whenWorkflowRunsForAdulteryCase_WithNamedCoRespondent_allTasksRun_payloadReturned() throws WorkflowException, TaskException {
        when(caseDataUtils.isAdulteryCaseWithNamedCoRespondent(payload)).thenReturn(true);

        Map<String, Object> response = ccdCallbackBulkPrintWorkflow.run(ccdCallbackRequestRequest, AUTH_TOKEN);
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
    public void whenWorkflowRunsForNonAdulteryCase_allTasksRunExceptForCoRespondent_payloadReturned() throws WorkflowException, TaskException {
        when(caseDataUtils.isAdulteryCaseWithNamedCoRespondent(payload)).thenReturn(false);

        Map<String, Object> response = ccdCallbackBulkPrintWorkflow.run(ccdCallbackRequestRequest, AUTH_TOKEN);
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
    public void whenWorkflowRunsForNonAdulteryCase_allTasksRunExceptForUpdateNoticeDetails_ToggledOff() throws WorkflowException, TaskException {
        when(featureToggleService.isFeatureEnabled(Features.REPRESENTED_RESPONDENT_JOURNEY)).thenReturn(false);
        when(caseDataUtils.isAdulteryCaseWithNamedCoRespondent(payload)).thenReturn(true);

        Map<String, Object> response = ccdCallbackBulkPrintWorkflow.run(ccdCallbackRequestRequest, AUTH_TOKEN);
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
    public void whenWorkflowRunsForNonAdulteryCase_allTasksRunExceptForUpdateNoticeDetails_NotAosEvent() throws WorkflowException, TaskException {
        CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .caseData(payload)
            .build();

        ccdCallbackRequestRequest =
            CcdCallbackRequest.builder()
                .eventId(TEST_EVENT_ID)
                .token(TEST_TOKEN)
                .caseDetails(caseDetails)
                .build();

        when(caseDataUtils.isAdulteryCaseWithNamedCoRespondent(payload)).thenReturn(true);

        Map<String, Object> response = ccdCallbackBulkPrintWorkflow.run(ccdCallbackRequestRequest, AUTH_TOKEN);
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
    public void whenWorkflowRunsForNonAdulteryCase_allTasksRunExceptForUpdateNoticeDetails_RespSolNotDigital()
        throws WorkflowException, TaskException {
        payload = new HashMap<>();

        CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .caseData(payload)
            .build();

        ccdCallbackRequestRequest =
            CcdCallbackRequest.builder()
                .eventId(ISSUE_AOS)
                .token(TEST_TOKEN)
                .caseDetails(caseDetails)
                .build();

        context.setTransientObject(CASE_DETAILS_JSON_KEY, ccdCallbackRequestRequest.getCaseDetails());

        when(caseDataUtils.isAdulteryCaseWithNamedCoRespondent(payload)).thenReturn(true);

        Map<String, Object> response = ccdCallbackBulkPrintWorkflow.run(ccdCallbackRequestRequest, AUTH_TOKEN);
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