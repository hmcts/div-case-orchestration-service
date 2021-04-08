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
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.PetitionerSolicitorApplicationSubmittedEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ProcessPbaPaymentTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RemoveMiniPetitionDraftDocumentsTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendPetitionerSubmissionNotificationEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateRespondentDigitalDetailsTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ValidateSolicitorCaseDataTask;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.RESPONDENT_SOLICITOR_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PREVIOUS_CASE_ID_CCD_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.CaseDataTestHelper.buildOrganisationPolicy;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.mockTasksExecution;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTaskWasNeverCalled;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksCalledInOrder;

@RunWith(MockitoJUnitRunner.class)
public class SolicitorSubmissionWorkflowTest {

    @Mock
    private ValidateSolicitorCaseDataTask validateSolicitorCaseDataTask;

    @Mock
    private ProcessPbaPaymentTask processPbaPaymentTask;

    @Mock
    private RemoveMiniPetitionDraftDocumentsTask removeMiniPetitionDraftDocumentsTask;

    @Mock
    private SendPetitionerSubmissionNotificationEmailTask sendPetitionerSubmissionNotificationEmailTask;

    @Mock
    private PetitionerSolicitorApplicationSubmittedEmailTask petitionerSolicitorApplicationSubmittedEmailTask;

    @Mock
    private UpdateRespondentDigitalDetailsTask updateRespondentDigitalDetailsTask;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private SolicitorSubmissionWorkflow solicitorSubmissionWorkflow;

    private CcdCallbackRequest ccdCallbackRequestRequest;
    private Map<String, Object> caseData;

    @Before
    public void setup() {
        caseData = new HashMap<>();
        ccdCallbackRequestRequest = CcdCallbackRequest.builder()
            .eventId(TEST_EVENT_ID)
            .token(TEST_TOKEN)
            .caseDetails(CaseDetails.builder()
                .caseId(TEST_CASE_ID)
                .state(TEST_STATE)
                .caseData(caseData)
                .build())
            .build();
    }

    @Test
    public void executeNonFeatureToggledTasksAndReturnPayloadWhenRespondentJourneyToggledOff() throws Exception {
        disabledRespondentJourney();
        givenRespondentSolicitorDigital();
        runTestExecuteAllTasksButPetSolEmailAndRespDigAreCalled();
    }

    @Test
    public void executeTasksButNotPetSolEmailAndReturnPayloadWhenPetitionerIsNotRepresented() throws Exception {
        enabledRespondentJourney();
        givenRespondentSolicitorDigital();
        runTestExecuteAllTasksButPetSolEmailAreCalled();
    }

    @Test
    public void executeTasksButNotPetSolEmailAndReturnPayloadWhenCaseIsAmended() throws Exception {
        enabledRespondentJourney();
        givenRespondentSolicitorDigital();
        givenAmendedCase();

        runTestExecuteAllTasksButPetSolEmailAreCalled();
    }

    @Test
    public void executeAllTasksAndReturnPayloadWhenRespondentJourneyEnabledAndPetRepresentedAndCaseNotAmended() throws Exception {
        enabledRespondentJourney();
        givenPetitionerRepresented();
        givenRespondentSolicitorDigital();

        runTestAllTasksAreCalled();
    }

    @Test
    public void executeAllTasksButNotUpdateRespDigAndReturnPayloadWhenRespSolNotDigital() throws Exception {
        enabledRespondentJourney();
        givenPetitionerRepresented();

        runTestExecuteAllTasksButUpdateRespDigAreCalled();
    }

    private void runTestAllTasksAreCalled() throws WorkflowException {
        mockTasksExecution(
            caseData,
            validateSolicitorCaseDataTask,
            processPbaPaymentTask,
            removeMiniPetitionDraftDocumentsTask,
            sendPetitionerSubmissionNotificationEmailTask,
            petitionerSolicitorApplicationSubmittedEmailTask,
            updateRespondentDigitalDetailsTask
        );

        assertThat(solicitorSubmissionWorkflow.run(ccdCallbackRequestRequest, AUTH_TOKEN), is(caseData));

        verifyTasksCalledInOrder(
            caseData,
            validateSolicitorCaseDataTask,
            processPbaPaymentTask,
            removeMiniPetitionDraftDocumentsTask,
            sendPetitionerSubmissionNotificationEmailTask,
            petitionerSolicitorApplicationSubmittedEmailTask,
            updateRespondentDigitalDetailsTask
        );
    }

    private void givenAmendedCase() {
        caseData.put(PREVIOUS_CASE_ID_CCD_KEY, new Object());
    }

    private void givenRespondentSolicitorDigital() {
        caseData.put(RESPONDENT_SOLICITOR_ORGANISATION_POLICY, buildOrganisationPolicy());
    }

    private void givenPetitionerRepresented() {
        caseData.put(PETITIONER_SOLICITOR_EMAIL, TEST_SOLICITOR_EMAIL);
    }

    private void runTestExecuteAllTasksButPetSolEmailAreCalled() throws WorkflowException {
        mockTasksExecution(
            caseData,
            validateSolicitorCaseDataTask,
            processPbaPaymentTask,
            removeMiniPetitionDraftDocumentsTask,
            sendPetitionerSubmissionNotificationEmailTask,
            updateRespondentDigitalDetailsTask
        );

        assertThat(solicitorSubmissionWorkflow.run(ccdCallbackRequestRequest, AUTH_TOKEN), is(caseData));

        verifyTasksCalledInOrder(
            caseData,
            validateSolicitorCaseDataTask,
            processPbaPaymentTask,
            removeMiniPetitionDraftDocumentsTask,
            sendPetitionerSubmissionNotificationEmailTask,
            updateRespondentDigitalDetailsTask
        );

        verifyTaskWasNeverCalled(petitionerSolicitorApplicationSubmittedEmailTask);
    }

    private void runTestExecuteAllTasksButPetSolEmailAndRespDigAreCalled() throws WorkflowException {
        mockTasksExecution(
            caseData,
            validateSolicitorCaseDataTask,
            processPbaPaymentTask,
            removeMiniPetitionDraftDocumentsTask,
            sendPetitionerSubmissionNotificationEmailTask
        );

        assertThat(solicitorSubmissionWorkflow.run(ccdCallbackRequestRequest, AUTH_TOKEN), is(caseData));

        verifyTasksCalledInOrder(
            caseData,
            validateSolicitorCaseDataTask,
            processPbaPaymentTask,
            removeMiniPetitionDraftDocumentsTask,
            sendPetitionerSubmissionNotificationEmailTask
        );

        verifyTaskWasNeverCalled(petitionerSolicitorApplicationSubmittedEmailTask);
        verifyTaskWasNeverCalled(updateRespondentDigitalDetailsTask);
    }

    private void runTestExecuteAllTasksButUpdateRespDigAreCalled() throws WorkflowException {
        mockTasksExecution(
            caseData,
            validateSolicitorCaseDataTask,
            processPbaPaymentTask,
            removeMiniPetitionDraftDocumentsTask,
            sendPetitionerSubmissionNotificationEmailTask,
            petitionerSolicitorApplicationSubmittedEmailTask
        );

        assertThat(solicitorSubmissionWorkflow.run(ccdCallbackRequestRequest, AUTH_TOKEN), is(caseData));

        verifyTasksCalledInOrder(
            caseData,
            validateSolicitorCaseDataTask,
            processPbaPaymentTask,
            removeMiniPetitionDraftDocumentsTask,
            sendPetitionerSubmissionNotificationEmailTask,
            petitionerSolicitorApplicationSubmittedEmailTask
        );

        verifyTaskWasNeverCalled(updateRespondentDigitalDetailsTask);
    }

    private void enabledRespondentJourney() {
        when(featureToggleService.isFeatureEnabled(Features.REPRESENTED_RESPONDENT_JOURNEY)).thenReturn(true);
    }

    private void disabledRespondentJourney() {
        when(featureToggleService.isFeatureEnabled(Features.REPRESENTED_RESPONDENT_JOURNEY)).thenReturn(false);
    }
}
