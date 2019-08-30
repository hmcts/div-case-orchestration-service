package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.TestConstants;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseFormatterAddDocuments;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GenericEmailNotification;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RespondentAnswersGenerator;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CO_RESPONDENT_NAMED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_INFERRED_RESPONDENT_GENDER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RELATIONSHIP_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RESP_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_TEMPLATE_VARS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PET_SOL_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PET_SOL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_CO_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_ADMIT_OR_CONSENT_TO_FACT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_AOS_2_YR_CONSENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_AOS_ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_SOL_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_WILL_DEFEND_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SEPARATION_2YRS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOL_AOS_RECEIVED_NO_ADCON_STARTED_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOL_AOS_SUBMITTED_DEFENDED_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOL_AOS_SUBMITTED_UNDEFENDED_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@RunWith(MockitoJUnitRunner.class)
public class RespondentSubmittedCallbackWorkflowUTest {

    @Mock
    private GenericEmailNotification emailNotificationTask;
    @Mock
    private RespondentAnswersGenerator respondentAnswersGenerator;
    @Mock
    private CaseFormatterAddDocuments caseFormatterAddDocuments;
    @Mock
    private CaseMaintenanceClient caseMaintenanceClient;

    @InjectMocks
    private RespondentSubmittedCallbackWorkflow classToTest;

    private final Map<String, Object> expectedData = new HashMap<>();

    @Test
    public void givenCaseNotDefended_whenRunWorkflow_thenEmailNotificationTaskCalled() throws WorkflowException {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(D_8_PETITIONER_FIRST_NAME, TestConstants.TEST_USER_FIRST_NAME);
        caseData.put(D_8_PETITIONER_LAST_NAME, TestConstants.TEST_USER_LAST_NAME);
        caseData.put(D_8_PETITIONER_EMAIL, TestConstants.TEST_USER_EMAIL);
        caseData.put(D_8_CASE_REFERENCE, TestConstants.TEST_CASE_FAMILY_MAN_ID);
        caseData.put(D_8_INFERRED_RESPONDENT_GENDER, "male");
        caseData.put(RESP_WILL_DEFEND_DIVORCE, NO_VALUE);

        CaseDetails caseDetails = CaseDetails.builder()
                .caseId(TEST_CASE_ID)
                .caseData(caseData)
                .build();
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        when(emailNotificationTask.execute(any(), any())).thenReturn(caseDetails.getCaseData());
        when(respondentAnswersGenerator.execute(any(), any())).thenReturn(caseDetails.getCaseData());
        when(caseFormatterAddDocuments.execute(any(), any())).thenReturn(caseDetails.getCaseData());

        Map<String, String> vars = ImmutableMap.of(
                NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, TestConstants.TEST_USER_FIRST_NAME,
                NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, TestConstants.TEST_USER_LAST_NAME,
                NOTIFICATION_RELATIONSHIP_KEY, "husband",
                NOTIFICATION_REFERENCE_KEY, TestConstants.TEST_CASE_FAMILY_MAN_ID
        );
        Map<String, Object> response = classToTest.run(ccdCallbackRequest, TestConstants.TEST_TOKEN);
        verify(emailNotificationTask, times(1))
                .execute(argThat(argument ->
                        argument.getTransientObject(ID).equals(TEST_CASE_ID)
                                && argument.getTransientObject(NOTIFICATION_TEMPLATE_VARS).equals(vars)), any());
        assertEquals(caseDetails.getCaseData(), response);
    }

    @Test
    public void givenCaseDefended_whenRunWorkflow_thenEmailNotificationTaskNotCalled() throws WorkflowException {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(RESP_WILL_DEFEND_DIVORCE, YES_VALUE);

        CaseDetails caseDetails = CaseDetails.builder()
                .caseId(TEST_CASE_ID)
                .caseData(caseData)
                .build();
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        when(respondentAnswersGenerator.execute(any(), any())).thenReturn(caseDetails.getCaseData());
        when(caseFormatterAddDocuments.execute(any(), any())).thenReturn(caseDetails.getCaseData());

        Map<String, Object> response = classToTest.run(ccdCallbackRequest, TestConstants.TEST_TOKEN);
        verifyNoMoreInteractions(emailNotificationTask);
        assertEquals(caseDetails.getCaseData(), response);
    }

    @Test
    public void givenCaseNoPetEmail_whenRunWorkflow_thenEmailNotificationTaskNotCalled() throws WorkflowException {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(D_8_PETITIONER_FIRST_NAME, TestConstants.TEST_USER_FIRST_NAME);
        caseData.put(D_8_PETITIONER_LAST_NAME, TestConstants.TEST_USER_LAST_NAME);
        caseData.put(D_8_CASE_REFERENCE, TestConstants.TEST_CASE_FAMILY_MAN_ID);
        caseData.put(D_8_INFERRED_RESPONDENT_GENDER, "male");
        caseData.put(RESP_WILL_DEFEND_DIVORCE, "No");


        CaseDetails caseDetails = CaseDetails.builder()
                .caseId(TEST_CASE_ID)
                .caseData(caseData)
                .build();
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        when(respondentAnswersGenerator.execute(any(), any())).thenReturn(caseDetails.getCaseData());
        when(caseFormatterAddDocuments.execute(any(), any())).thenReturn(caseDetails.getCaseData());

        Map<String, Object> response = classToTest.run(ccdCallbackRequest, TestConstants.TEST_TOKEN);
        verifyNoMoreInteractions(emailNotificationTask);
        assertEquals(caseDetails.getCaseData(), response);
    }

    @Test
    public void givenAdulteryCoRespNotRepliedRespUndefended_whenSendEmail_thenSendRespUndefendedCoRespNoReplyTemplate() throws Exception {
        Map<String, Object> caseData = new HashMap<>();

        caseData.put(D_8_PETITIONER_FIRST_NAME, TestConstants.TEST_USER_FIRST_NAME);
        caseData.put(D_8_PETITIONER_LAST_NAME, TestConstants.TEST_USER_LAST_NAME) ;
        caseData.put(D_8_PETITIONER_EMAIL, TestConstants.TEST_USER_EMAIL);
        caseData.put(D_8_CASE_REFERENCE, TestConstants.TEST_CASE_FAMILY_MAN_ID);
        caseData.put(D_8_INFERRED_RESPONDENT_GENDER, "male");
        caseData.put(D_8_CO_RESPONDENT_NAMED, YES_VALUE);
        caseData.put(RECEIVED_AOS_FROM_CO_RESP, NO_VALUE);

        CaseDetails caseDetails = CaseDetails.builder()
                .caseId(TEST_CASE_ID)
                .caseData(caseData)
                .build();

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        classToTest.run(ccdCallbackRequest, AUTH_TOKEN);

        ArgumentCaptor<TaskContext> argument = ArgumentCaptor.forClass(TaskContext.class);
        verify(emailNotificationTask).execute(argument.capture(), eq(ccdCallbackRequest.getCaseDetails().getCaseData()));

        TaskContext capturedTask = argument.getValue();

        DefaultTaskContext expectedContext = createExpectedContext(EmailTemplateNames.RESPONDENT_SUBMISSION_CONSENT_CORESP_NOT_REPLIED, false);

        assertThat(expectedContext, equalTo(capturedTask));
    }

    @Test
    public void givenAdulteryCoRespNotRepliedRespNoAdmitUndefended_whenSendEmail_thenSendRespNoAdmitUndefendedCoRespNoReplyTemplate() throws Exception {
        Map<String, Object> caseData = new HashMap<>();

        caseData.put(D_8_PETITIONER_FIRST_NAME, TestConstants.TEST_USER_FIRST_NAME);
        caseData.put(D_8_PETITIONER_LAST_NAME, TestConstants.TEST_USER_LAST_NAME) ;
        caseData.put(D_8_PETITIONER_EMAIL, TestConstants.TEST_USER_EMAIL);
        caseData.put(D_8_CASE_REFERENCE, TestConstants.TEST_CASE_FAMILY_MAN_ID);
        caseData.put(D_8_INFERRED_RESPONDENT_GENDER, "male");
        caseData.put(D_8_CO_RESPONDENT_NAMED, YES_VALUE);
        caseData.put(RECEIVED_AOS_FROM_CO_RESP, NO_VALUE);
        caseData.put(D_8_REASON_FOR_DIVORCE, ADULTERY);
        caseData.put(RESP_ADMIT_OR_CONSENT_TO_FACT, NO_VALUE);

        CaseDetails caseDetails = CaseDetails.builder()
                .caseId(TEST_CASE_ID)
                .caseData(caseData)
                .build();

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        classToTest.run(ccdCallbackRequest, AUTH_TOKEN);

        ArgumentCaptor<TaskContext> argument = ArgumentCaptor.forClass(TaskContext.class);
        verify(emailNotificationTask).execute(argument.capture(), eq(ccdCallbackRequest.getCaseDetails().getCaseData()));

        TaskContext capturedTask = argument.getValue();

        DefaultTaskContext expectedContext = createExpectedContext(
                EmailTemplateNames.AOS_RECEIVED_UNDEFENDED_NO_ADMIT_ADULTERY_CORESP_NOT_REPLIED, false);

        assertThat(expectedContext, equalTo(capturedTask));
    }

    @Test
    public void givenAdulteryRespNoAdmitUndefendedCoRespReplied_whenSendEmail_thenSendRespNoAdmitUndefendedTemplate() throws Exception {
        Map<String, Object> caseData = new HashMap<>();

        caseData.put(D_8_PETITIONER_FIRST_NAME, TestConstants.TEST_USER_FIRST_NAME);
        caseData.put(D_8_PETITIONER_LAST_NAME, TestConstants.TEST_USER_LAST_NAME) ;
        caseData.put(D_8_PETITIONER_EMAIL, TestConstants.TEST_USER_EMAIL);
        caseData.put(D_8_CASE_REFERENCE, TestConstants.TEST_CASE_FAMILY_MAN_ID);
        caseData.put(D_8_INFERRED_RESPONDENT_GENDER, "male");
        caseData.put(D_8_REASON_FOR_DIVORCE, ADULTERY);
        caseData.put(RESP_ADMIT_OR_CONSENT_TO_FACT, NO_VALUE);

        CaseDetails caseDetails = CaseDetails.builder()
                .caseId(TEST_CASE_ID)
                .caseData(caseData)
                .build();

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        classToTest.run(ccdCallbackRequest, AUTH_TOKEN);

        ArgumentCaptor<TaskContext> argument = ArgumentCaptor.forClass(TaskContext.class);
        verify(emailNotificationTask).execute(argument.capture(), eq(ccdCallbackRequest.getCaseDetails().getCaseData()));

        TaskContext capturedTask = argument.getValue();

        DefaultTaskContext expectedContext = createExpectedContext(EmailTemplateNames.AOS_RECEIVED_UNDEFENDED_NO_ADMIT_ADULTERY, false);

        assertThat(expectedContext, equalTo(capturedTask));
    }

    @Test
    public void givenSep2YrRespNoConsentUndefended_whenSendEmail_thenSendRespNoConsentUndefendedTemplate() throws Exception {
        Map<String, Object> caseData = new HashMap<>();

        caseData.put(D_8_PETITIONER_FIRST_NAME, TestConstants.TEST_USER_FIRST_NAME);
        caseData.put(D_8_PETITIONER_LAST_NAME, TestConstants.TEST_USER_LAST_NAME) ;
        caseData.put(D_8_PETITIONER_EMAIL, TestConstants.TEST_USER_EMAIL);
        caseData.put(D_8_CASE_REFERENCE, TestConstants.TEST_CASE_FAMILY_MAN_ID);
        caseData.put(D_8_INFERRED_RESPONDENT_GENDER, "male");
        caseData.put(D_8_REASON_FOR_DIVORCE, SEPARATION_2YRS);
        caseData.put(RESP_ADMIT_OR_CONSENT_TO_FACT, NO_VALUE);

        CaseDetails caseDetails = CaseDetails.builder()
                .caseId(TEST_CASE_ID)
                .caseData(caseData)
                .build();

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        classToTest.run(ccdCallbackRequest, AUTH_TOKEN);

        ArgumentCaptor<TaskContext> argument = ArgumentCaptor.forClass(TaskContext.class);
        verify(emailNotificationTask).execute(argument.capture(), eq(ccdCallbackRequest.getCaseDetails().getCaseData()));

        TaskContext capturedTask = argument.getValue();

        DefaultTaskContext expectedContext = createExpectedContext(EmailTemplateNames.AOS_RECEIVED_UNDEFENDED_NO_CONSENT_2_YEARS, false);

        assertThat(expectedContext, equalTo(capturedTask));
    }

    @Test
    public void givenAdulteryCoRespRepliedRespUndefended_whenSendEmail_thenSendRespUndefendedTemplate() throws Exception {
        Map<String, Object> caseData = new HashMap<>();

        caseData.put(D_8_PETITIONER_FIRST_NAME, TestConstants.TEST_USER_FIRST_NAME);
        caseData.put(D_8_PETITIONER_LAST_NAME, TestConstants.TEST_USER_LAST_NAME) ;
        caseData.put(D_8_PETITIONER_EMAIL, TestConstants.TEST_USER_EMAIL);
        caseData.put(D_8_CASE_REFERENCE, TestConstants.TEST_CASE_FAMILY_MAN_ID);
        caseData.put(D_8_INFERRED_RESPONDENT_GENDER, "male");
        caseData.put(D_8_CO_RESPONDENT_NAMED, YES_VALUE);
        caseData.put(RECEIVED_AOS_FROM_CO_RESP, YES_VALUE);

        CaseDetails caseDetails = CaseDetails.builder()
                .caseId(TEST_CASE_ID)
                .caseData(caseData)
                .build();

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        classToTest.run(ccdCallbackRequest, AUTH_TOKEN);

        ArgumentCaptor<TaskContext> argument = ArgumentCaptor.forClass(TaskContext.class);
        verify(emailNotificationTask).execute(argument.capture(), eq(ccdCallbackRequest.getCaseDetails().getCaseData()));

        TaskContext capturedTask = argument.getValue();

        DefaultTaskContext expectedContext = createExpectedContext(EmailTemplateNames.RESPONDENT_SUBMISSION_CONSENT, false);

        assertThat(expectedContext, equalTo(capturedTask));
    }

    @Test
    public void givenCaseSolicitor_whenRunWorkflow_thenSolEmailNotificationTaskCalled() throws WorkflowException {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(D_8_PETITIONER_FIRST_NAME, TestConstants.TEST_PETITIONER_FIRST_NAME);
        caseData.put(D_8_PETITIONER_LAST_NAME, TestConstants.TEST_PETITIONER_LAST_NAME) ;
        caseData.put(PET_SOL_EMAIL, TestConstants.TEST_USER_EMAIL);
        caseData.put(D_8_CASE_REFERENCE, TestConstants.TEST_CASE_FAMILY_MAN_ID);
        caseData.put(RESP_FIRST_NAME_CCD_FIELD, TestConstants.TEST_USER_FIRST_NAME);
        caseData.put(RESP_LAST_NAME_CCD_FIELD, TestConstants.TEST_USER_LAST_NAME);
        caseData.put(PET_SOL_NAME, TestConstants.TEST_SOLICITOR_NAME);

        CaseDetails caseDetails = CaseDetails.builder()
                .caseId(TEST_CASE_ID)
                .caseData(caseData)
                .build();

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        classToTest.run(ccdCallbackRequest, AUTH_TOKEN);

        ArgumentCaptor<TaskContext> argument = ArgumentCaptor.forClass(TaskContext.class);
        verify(emailNotificationTask).execute(argument.capture(), eq(ccdCallbackRequest.getCaseDetails().getCaseData()));

        TaskContext capturedTask = argument.getValue();

        DefaultTaskContext expectedContext = createExpectedContext(EmailTemplateNames.SOL_APPLICANT_AOS_RECEIVED, true);

        assertThat(expectedContext, equalTo(capturedTask));
    }

    @Test
    public void correctlyMapFieldsInCaseData_When_2yearSepAndRespAos2yrConsentIsYes() throws WorkflowException {
        // When Fact = 2 year separation and RespAOS2yrConsent = Yes - set RespAdmitOrConsentToFact = "Yes" and RespWillDefendDivorce = "No"

        expectedData.put(RESP_SOL_REPRESENTED, YES_VALUE);
        expectedData.put(D_8_REASON_FOR_DIVORCE, SEPARATION_2YRS);
        expectedData.put(RESP_AOS_2_YR_CONSENT, YES_VALUE);
        expectedData.put(RESP_ADMIT_OR_CONSENT_TO_FACT, YES_VALUE);
        expectedData.put(RESP_WILL_DEFEND_DIVORCE, NO_VALUE);

        Map<String, Object> caseData = buildSolicitorResponse(SEPARATION_2YRS, true);

        CaseDetails caseDetails = CaseDetails.builder()
                .caseId(TEST_CASE_ID)
                .caseData(caseData)
                .build();
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        when(respondentAnswersGenerator.execute(any(), any())).thenReturn(caseDetails.getCaseData());
        when(caseFormatterAddDocuments.execute(any(), any())).thenReturn(caseDetails.getCaseData());

        Map<String, Object> response = classToTest.run(ccdCallbackRequest, TestConstants.TEST_TOKEN);

        assertEquals(expectedData, response);
    }

    @Test
    public void correctlyMapFieldsInCaseData_When_AdulteryAndRespAosAdulteryIsYes() throws WorkflowException {
        // When Fact = adultery and RespAOSAdultery = Yes - set RespAdmitOrConsentToFact = "Yes" and RespWillDefendDivorce = "No"

        expectedData.put(RESP_SOL_REPRESENTED, YES_VALUE);
        expectedData.put(D_8_REASON_FOR_DIVORCE, ADULTERY);
        expectedData.put(RESP_AOS_ADULTERY, YES_VALUE);
        expectedData.put(RESP_ADMIT_OR_CONSENT_TO_FACT, YES_VALUE);
        expectedData.put(RESP_WILL_DEFEND_DIVORCE, NO_VALUE);

        Map<String, Object> caseData = buildSolicitorResponse(ADULTERY, true);

        CaseDetails caseDetails = CaseDetails.builder()
                .caseId(TEST_CASE_ID)
                .caseData(caseData)
                .build();
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        when(respondentAnswersGenerator.execute(any(), any())).thenReturn(caseDetails.getCaseData());
        when(caseFormatterAddDocuments.execute(any(), any())).thenReturn(caseDetails.getCaseData());

        Map<String, Object> response = classToTest.run(ccdCallbackRequest, TestConstants.TEST_TOKEN);

        assertEquals(expectedData, response);
    }

    @Test
    public void givenSolicitorIsRepresenting_ConsentAndDefended_then_eventTriggeredIs_SolAosSubmittedDefended() throws WorkflowException {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(RESP_SOL_REPRESENTED, YES_VALUE);
        caseData.put(RESP_ADMIT_OR_CONSENT_TO_FACT, YES_VALUE);
        caseData.put(RESP_WILL_DEFEND_DIVORCE, YES_VALUE);

        CaseDetails caseDetails = CaseDetails.builder()
                .caseId(TEST_CASE_ID)
                .caseData(caseData)
                .build();

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        classToTest.run(ccdCallbackRequest, AUTH_TOKEN);

        verify(caseMaintenanceClient).updateCase(AUTH_TOKEN, TEST_CASE_ID, SOL_AOS_SUBMITTED_DEFENDED_EVENT_ID, caseData);
    }

    @Test
    public void givenSolicitorIsRepresenting_ConsentAndNotDefended_then_eventTriggeredIs_SolAosSubmittedUndefended() throws WorkflowException {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(RESP_SOL_REPRESENTED, YES_VALUE);
        caseData.put(RESP_ADMIT_OR_CONSENT_TO_FACT, YES_VALUE);
        caseData.put(RESP_WILL_DEFEND_DIVORCE, NO_VALUE);

        CaseDetails caseDetails = CaseDetails.builder()
                .caseId(TEST_CASE_ID)
                .caseData(caseData)
                .build();

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        classToTest.run(ccdCallbackRequest, AUTH_TOKEN);

        verify(caseMaintenanceClient).updateCase(AUTH_TOKEN, TEST_CASE_ID, SOL_AOS_SUBMITTED_UNDEFENDED_EVENT_ID, caseData);
    }

    @Test
    public void givenSolicitorIsRepresenting_NoConsentAndDefended_then_eventTriggeredIs_SolAosReceivedNoAdConStarted() throws WorkflowException {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(RESP_SOL_REPRESENTED, YES_VALUE);
        caseData.put(RESP_ADMIT_OR_CONSENT_TO_FACT, NO_VALUE);
        caseData.put(RESP_WILL_DEFEND_DIVORCE, YES_VALUE);

        CaseDetails caseDetails = CaseDetails.builder()
                .caseId(TEST_CASE_ID)
                .caseData(caseData)
                .build();

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        classToTest.run(ccdCallbackRequest, AUTH_TOKEN);

        verify(caseMaintenanceClient).updateCase(AUTH_TOKEN, TEST_CASE_ID, SOL_AOS_RECEIVED_NO_ADCON_STARTED_EVENT_ID, caseData);
    }

    @Test
    public void givenSolicitorIsRepresenting_NoConsentAndNotDefended_then_eventTriggeredIs_solAosSubmittedUndefended() throws WorkflowException {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(RESP_SOL_REPRESENTED, YES_VALUE);
        caseData.put(RESP_ADMIT_OR_CONSENT_TO_FACT, NO_VALUE);
        caseData.put(RESP_WILL_DEFEND_DIVORCE, NO_VALUE);

        CaseDetails caseDetails = CaseDetails.builder()
                .caseId(TEST_CASE_ID)
                .caseData(caseData)
                .build();

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        classToTest.run(ccdCallbackRequest, AUTH_TOKEN);

        verify(caseMaintenanceClient).updateCase(AUTH_TOKEN, TEST_CASE_ID, SOL_AOS_RECEIVED_NO_ADCON_STARTED_EVENT_ID, caseData);
    }

    private DefaultTaskContext createExpectedContext(EmailTemplateNames template, boolean isSolicitor) {

        Map<String, Object> expectedTemplateVars = new HashMap<>();
        DefaultTaskContext expectedContext = new DefaultTaskContext();
        if (isSolicitor) {
            expectedTemplateVars.put(NOTIFICATION_EMAIL, TestConstants.TEST_USER_EMAIL);
            expectedTemplateVars.put(NOTIFICATION_PET_NAME, TestConstants.TEST_PETITIONER_FIRST_NAME + " " + TestConstants.TEST_PETITIONER_LAST_NAME);
            expectedTemplateVars.put(NOTIFICATION_RESP_NAME, TestConstants.TEST_USER_FIRST_NAME + " " + TestConstants.TEST_USER_LAST_NAME);
            expectedTemplateVars.put(NOTIFICATION_SOLICITOR_NAME, TestConstants.TEST_SOLICITOR_NAME);
            expectedTemplateVars.put(NOTIFICATION_CCD_REFERENCE_KEY, TEST_CASE_ID);

            expectedContext.setTransientObjects(ImmutableMap
                    .of(NOTIFICATION_EMAIL, TestConstants.TEST_USER_EMAIL,
                            AUTH_TOKEN_JSON_KEY, AUTH_TOKEN,
                            NOTIFICATION_TEMPLATE_VARS, expectedTemplateVars,
                            NOTIFICATION_TEMPLATE, template,
                            ID, TEST_CASE_ID
                    ));
        } else {
            expectedTemplateVars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, TestConstants.TEST_USER_FIRST_NAME);
            expectedTemplateVars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, TestConstants.TEST_USER_LAST_NAME);
            expectedTemplateVars.put(NOTIFICATION_RELATIONSHIP_KEY, "husband");
            expectedTemplateVars.put(NOTIFICATION_REFERENCE_KEY, TestConstants.TEST_CASE_FAMILY_MAN_ID);

            expectedContext.setTransientObjects(ImmutableMap
                    .of(NOTIFICATION_EMAIL, TestConstants.TEST_USER_EMAIL,
                            AUTH_TOKEN_JSON_KEY, AUTH_TOKEN,
                            NOTIFICATION_TEMPLATE_VARS, expectedTemplateVars,
                            NOTIFICATION_TEMPLATE, template,
                            ID, TEST_CASE_ID
                    ));
        }
        return expectedContext;
    }

    private Map<String, Object> buildSolicitorResponse(String reasonForDivorce, boolean consented) {
        Map<String, Object> caseData = new HashMap<>();

        caseData.put(D_8_REASON_FOR_DIVORCE, reasonForDivorce);
        caseData.put(RESP_SOL_REPRESENTED, YES_VALUE);

        if ((reasonForDivorce.equalsIgnoreCase(SEPARATION_2YRS)) && consented) {
            caseData.put(RESP_AOS_2_YR_CONSENT, YES_VALUE);
        } else if ((reasonForDivorce.equalsIgnoreCase(ADULTERY)) && consented) {
            caseData.put(RESP_AOS_ADULTERY, YES_VALUE);
        }

        return caseData;
    }
}