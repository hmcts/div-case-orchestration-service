package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.TestConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GenericEmailNotification;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.QueueAosSolicitorSubmitTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendRespondentSubmissionNotificationForDefendedDivorceEmail;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendRespondentSubmissionNotificationForUndefendedDivorceEmail;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CO_RESPONDENT_NAMED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_INFERRED_RESPONDENT_GENDER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE;
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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_WELSH_HUSBAND_OR_WIFE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PET_SOL_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PET_SOL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_CO_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_ADMIT_OR_CONSENT_TO_FACT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_WILL_DEFEND_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.SEPARATION_TWO_YEARS;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;

@RunWith(MockitoJUnitRunner.class)
public class AosSubmissionWorkflowTest {

    private static final String UNFORMATTED_CASE_ID = "0123456789012345";
    private static final String RESP_ACKNOWLEDGES_SERVICE_DEFENDING_DIVORCE_JSON =
            "/jsonExamples/payloads/respondentAcknowledgesServiceDefendingDivorce.json";
    private static final String RESP_ACKNOWLEDGES_SERVICE__NOT_DEFENDING_DIVORCE_JSON =
            "/jsonExamples/payloads/respondentAcknowledgesServiceNotDefendingDivorce.json";
    private static final String RESP_ACKNOWLEDGES_SERVICE__NOT_DEFENDING__NOT_ADMITTING_DIVORCE_JSON =
            "/jsonExamples/payloads/respondentAcknowledgesServiceNotDefendingNotAdmittingDivorce.json";
    private static final String UNCLEAR_ACKNOWLEDGEMENT_OF_SERVICE_JSON =
            "/jsonExamples/payloads/unclearAcknowledgementOfService.json";
    private static final String AOS_SOLICITOR_NOMINATED_JSON =
            "/jsonExamples/payloads/aosSolicitorNominated.json";
    private static final String AOS_SOLICITOR_NOMINATED_WITHOUT_FIELDS_SET_JSON =
            "/jsonExamples/payloads/aosSolicitorNominatedWithoutFieldSet.json";

    private Map<String, Object> returnedPayloadFromTask;

    @Rule
    public ExpectedException expectedException = none();

    @Mock
    private SendRespondentSubmissionNotificationForDefendedDivorceEmail defendedDivorceNotificationEmailTask;

    @Mock
    private SendRespondentSubmissionNotificationForUndefendedDivorceEmail undefendedDivorceNotificationEmailTask;

    @Mock
    private QueueAosSolicitorSubmitTask queueAosSolicitorSubmitTask;

    @Mock
    private GenericEmailNotification emailNotificationTask;

    @Captor
    private ArgumentCaptor<TaskContext> taskContextArgumentCaptor;

    @InjectMocks
    private AosSubmissionWorkflow aosSubmissionWorkflow;

    @Before
    public void setUp() throws TaskException {
        returnedPayloadFromTask = new HashMap<>();
        when(defendedDivorceNotificationEmailTask.execute(any(), any())).thenReturn(returnedPayloadFromTask);
        when(undefendedDivorceNotificationEmailTask.execute(any(), any())).thenReturn(returnedPayloadFromTask);
        when(queueAosSolicitorSubmitTask.execute(any(), any())).thenReturn(returnedPayloadFromTask);
    }

    @Test
    public void testDefendedTaskIsCalledWhenWorkflowIsRun() throws WorkflowException, IOException, TaskException {
        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(
                RESP_ACKNOWLEDGES_SERVICE_DEFENDING_DIVORCE_JSON, CcdCallbackRequest.class);
        Map<String, Object> caseData = ccdCallbackRequest.getCaseDetails().getCaseData();

        Map<String, Object> returnedPayloadFromWorkflow = aosSubmissionWorkflow.run(ccdCallbackRequest, AUTH_TOKEN);

        verify(defendedDivorceNotificationEmailTask).execute(taskContextArgumentCaptor.capture(), same(caseData));
        verifyZeroInteractions(undefendedDivorceNotificationEmailTask);
        assertThat(returnedPayloadFromWorkflow, is(sameInstance(returnedPayloadFromTask)));
        TaskContext taskContextPassedToTask = taskContextArgumentCaptor.getValue();
        String caseIdPassedToTask = taskContextPassedToTask.getTransientObject(CASE_ID_JSON_KEY);
        assertThat(caseIdPassedToTask, is(equalTo(UNFORMATTED_CASE_ID)));
    }

    @Test
    public void testUndefendedTaskIsCalled_WhenRespondentChoosesToNotDefendDivorce() throws IOException,
            WorkflowException, TaskException {
        CcdCallbackRequest callbackRequest = getJsonFromResourceFile(
                RESP_ACKNOWLEDGES_SERVICE__NOT_DEFENDING_DIVORCE_JSON, CcdCallbackRequest.class);
        Map<String, Object> caseData = callbackRequest.getCaseDetails().getCaseData();

        Map<String, Object> returnedPayloadFromWorkflow = aosSubmissionWorkflow.run(callbackRequest, AUTH_TOKEN);

        verify(undefendedDivorceNotificationEmailTask).execute(taskContextArgumentCaptor.capture(), same(caseData));
        verifyZeroInteractions(defendedDivorceNotificationEmailTask);
        assertThat(returnedPayloadFromWorkflow, is(sameInstance(returnedPayloadFromTask)));
        TaskContext taskContextPassedToTask = taskContextArgumentCaptor.getValue();
        String caseIdPassedToTask = taskContextPassedToTask.getTransientObject(CASE_ID_JSON_KEY);
        assertThat(caseIdPassedToTask, is(equalTo(UNFORMATTED_CASE_ID)));
    }

    @Test
    public void testUndefendedTaskIsCalled_WhenRespondentChoosesToNotDefendDivorceButNotAdmitWhatIsSaid()
            throws IOException, WorkflowException, TaskException {
        CcdCallbackRequest callbackRequest = getJsonFromResourceFile(
                RESP_ACKNOWLEDGES_SERVICE__NOT_DEFENDING__NOT_ADMITTING_DIVORCE_JSON, CcdCallbackRequest.class);
        Map<String, Object> caseData = callbackRequest.getCaseDetails().getCaseData();

        Map<String, Object> returnedPayloadFromWorkflow = aosSubmissionWorkflow.run(callbackRequest, AUTH_TOKEN);

        verify(undefendedDivorceNotificationEmailTask).execute(taskContextArgumentCaptor.capture(), same(caseData));
        verifyZeroInteractions(defendedDivorceNotificationEmailTask);
        assertThat(returnedPayloadFromWorkflow, is(sameInstance(returnedPayloadFromTask)));
        TaskContext taskContextPassedToTask = taskContextArgumentCaptor.getValue();
        String caseIdPassedToTask = taskContextPassedToTask.getTransientObject(CASE_ID_JSON_KEY);
        assertThat(caseIdPassedToTask, is(equalTo(UNFORMATTED_CASE_ID)));
    }

    @Test
    public void testExceptionIsThrown_IfNotPossibleToAssert_WhetherDivorceWillBeDefended() throws IOException,
            WorkflowException {
        expectedException.expect(WorkflowException.class);
        expectedException.expectMessage(String.format("%s field doesn't contain a valid value",
            RESP_WILL_DEFEND_DIVORCE));

        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(
                UNCLEAR_ACKNOWLEDGEMENT_OF_SERVICE_JSON, CcdCallbackRequest.class);
        Map<String, Object> incomingCaseDate = ccdCallbackRequest.getCaseDetails().getCaseData();

        Map<String, Object> returnedPayloadFromWorkflow = aosSubmissionWorkflow.run(ccdCallbackRequest, AUTH_TOKEN);

        assertThat(returnedPayloadFromWorkflow.size(), is(incomingCaseDate.size() + 1));
    }

    @Test
    public void testSolicitorTaskIsCalledWhenWorkflowIsRun_whenSolicitorIsRepresenting()
            throws WorkflowException, IOException, TaskException {

        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(
                AOS_SOLICITOR_NOMINATED_JSON, CcdCallbackRequest.class);
        Map<String, Object> caseData = ccdCallbackRequest.getCaseDetails().getCaseData();

        aosSubmissionWorkflow.run(ccdCallbackRequest, AUTH_TOKEN);

        verify(queueAosSolicitorSubmitTask).execute(taskContextArgumentCaptor.capture(), same(caseData));
    }

    @Test
    public void testSolicitorTaskIsNotCalledWhenSolicitorIsNotRepresenting() throws IOException,
            WorkflowException {

        CcdCallbackRequest callbackRequest = getJsonFromResourceFile(
                RESP_ACKNOWLEDGES_SERVICE__NOT_DEFENDING_DIVORCE_JSON, CcdCallbackRequest.class);

        aosSubmissionWorkflow.run(callbackRequest, AUTH_TOKEN);

        verifyZeroInteractions(queueAosSolicitorSubmitTask);
    }

    @Test
    public void testSolicitorTaskIsCalled_whenSolicitorIsRepresentingIsEmpty_andRespSolValuesExist()
            throws WorkflowException, IOException, TaskException {

        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(
                AOS_SOLICITOR_NOMINATED_WITHOUT_FIELDS_SET_JSON, CcdCallbackRequest.class);
        Map<String, Object> caseData = ccdCallbackRequest.getCaseDetails().getCaseData();

        aosSubmissionWorkflow.run(ccdCallbackRequest, AUTH_TOKEN);

        verify(queueAosSolicitorSubmitTask).execute(taskContextArgumentCaptor.capture(), same(caseData));
    }

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
            .caseId(TestConstants.TEST_CASE_ID)
            .caseData(caseData)
            .build();
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        when(emailNotificationTask.execute(any(), any())).thenReturn(caseDetails.getCaseData());

        Map<String, String> vars = ImmutableMap.of(
            NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, TestConstants.TEST_USER_FIRST_NAME,
            NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, TestConstants.TEST_USER_LAST_NAME,
            NOTIFICATION_RELATIONSHIP_KEY, "husband",
            NOTIFICATION_REFERENCE_KEY, TestConstants.TEST_CASE_FAMILY_MAN_ID,
            NOTIFICATION_WELSH_HUSBAND_OR_WIFE, TestConstants.TEST_WELSH_MALE_GENDER_IN_RELATION
        );
        aosSubmissionWorkflow.run(ccdCallbackRequest, AUTH_TOKEN);
        verify(emailNotificationTask, times(1))
            .execute(argThat(argument ->
                argument.getTransientObject(CASE_ID_JSON_KEY).equals(TestConstants.TEST_CASE_ID)
                    && argument.getTransientObject(NOTIFICATION_TEMPLATE_VARS).equals(vars)), any());
    }

    @Test
    public void givenCaseDefended_whenRunWorkflow_thenEmailNotificationTaskNotCalled() throws WorkflowException {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(RESP_WILL_DEFEND_DIVORCE, YES_VALUE);

        CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TestConstants.TEST_CASE_ID)
            .caseData(caseData)
            .build();
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        aosSubmissionWorkflow.run(ccdCallbackRequest, AUTH_TOKEN);
        verifyNoMoreInteractions(emailNotificationTask);
    }

    @Test
    public void givenCaseNoPetEmail_whenRunWorkflow_thenEmailNotificationTaskNotCalled() throws WorkflowException {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(D_8_PETITIONER_FIRST_NAME, TestConstants.TEST_USER_FIRST_NAME);
        caseData.put(D_8_PETITIONER_LAST_NAME, TestConstants.TEST_USER_LAST_NAME);
        caseData.put(D_8_CASE_REFERENCE, TestConstants.TEST_CASE_FAMILY_MAN_ID);
        caseData.put(D_8_INFERRED_RESPONDENT_GENDER, "male");
        caseData.put(RESP_WILL_DEFEND_DIVORCE, NO_VALUE);

        CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TestConstants.TEST_CASE_ID)
            .caseData(caseData)
            .build();
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        aosSubmissionWorkflow.run(ccdCallbackRequest, AUTH_TOKEN);
        verifyNoMoreInteractions(emailNotificationTask);
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
        caseData.put(RESP_WILL_DEFEND_DIVORCE, NO_VALUE);

        CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TestConstants.TEST_CASE_ID)
            .caseData(caseData)
            .build();

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        aosSubmissionWorkflow.run(ccdCallbackRequest, AUTH_TOKEN);

        ArgumentCaptor<TaskContext> argument = ArgumentCaptor.forClass(TaskContext.class);
        verify(emailNotificationTask).execute(argument.capture(), eq(ccdCallbackRequest.getCaseDetails().getCaseData()));

        TaskContext capturedTask = argument.getValue();

        DefaultTaskContext expectedContext = createdExpectedContext(EmailTemplateNames.RESPONDENT_SUBMISSION_CONSENT_CORESP_NOT_REPLIED, false);

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
        caseData.put(D_8_REASON_FOR_DIVORCE, ADULTERY.getValue());
        caseData.put(RESP_ADMIT_OR_CONSENT_TO_FACT, NO_VALUE);
        caseData.put(RESP_WILL_DEFEND_DIVORCE, NO_VALUE);

        CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TestConstants.TEST_CASE_ID)
            .caseData(caseData)
            .build();

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        aosSubmissionWorkflow.run(ccdCallbackRequest, AUTH_TOKEN);

        ArgumentCaptor<TaskContext> argument = ArgumentCaptor.forClass(TaskContext.class);
        verify(emailNotificationTask).execute(argument.capture(), eq(ccdCallbackRequest.getCaseDetails().getCaseData()));

        TaskContext capturedTask = argument.getValue();

        DefaultTaskContext expectedContext = createdExpectedContext(
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
        caseData.put(D_8_REASON_FOR_DIVORCE, ADULTERY.getValue());
        caseData.put(RESP_ADMIT_OR_CONSENT_TO_FACT, NO_VALUE);
        caseData.put(RESP_WILL_DEFEND_DIVORCE, NO_VALUE);

        CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TestConstants.TEST_CASE_ID)
            .caseData(caseData)
            .build();

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        aosSubmissionWorkflow.run(ccdCallbackRequest, AUTH_TOKEN);

        ArgumentCaptor<TaskContext> argument = ArgumentCaptor.forClass(TaskContext.class);
        verify(emailNotificationTask).execute(argument.capture(), eq(ccdCallbackRequest.getCaseDetails().getCaseData()));

        TaskContext capturedTask = argument.getValue();

        DefaultTaskContext expectedContext = createdExpectedContext(EmailTemplateNames.AOS_RECEIVED_UNDEFENDED_NO_ADMIT_ADULTERY, false);

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
        caseData.put(D_8_REASON_FOR_DIVORCE, SEPARATION_TWO_YEARS.getValue());
        caseData.put(RESP_ADMIT_OR_CONSENT_TO_FACT, NO_VALUE);
        caseData.put(RESP_WILL_DEFEND_DIVORCE, NO_VALUE);

        CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TestConstants.TEST_CASE_ID)
            .caseData(caseData)
            .build();

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        aosSubmissionWorkflow.run(ccdCallbackRequest, AUTH_TOKEN);

        ArgumentCaptor<TaskContext> argument = ArgumentCaptor.forClass(TaskContext.class);
        verify(emailNotificationTask).execute(argument.capture(), eq(ccdCallbackRequest.getCaseDetails().getCaseData()));

        TaskContext capturedTask = argument.getValue();

        DefaultTaskContext expectedContext = createdExpectedContext(EmailTemplateNames.AOS_RECEIVED_UNDEFENDED_NO_CONSENT_2_YEARS, false);

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
        caseData.put(RESP_WILL_DEFEND_DIVORCE, NO_VALUE);

        CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TestConstants.TEST_CASE_ID)
            .caseData(caseData)
            .build();

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        aosSubmissionWorkflow.run(ccdCallbackRequest, AUTH_TOKEN);

        ArgumentCaptor<TaskContext> argument = ArgumentCaptor.forClass(TaskContext.class);
        verify(emailNotificationTask).execute(argument.capture(), eq(ccdCallbackRequest.getCaseDetails().getCaseData()));

        TaskContext capturedTask = argument.getValue();

        DefaultTaskContext expectedContext = createdExpectedContext(EmailTemplateNames.RESPONDENT_SUBMISSION_CONSENT, false);

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
        caseData.put(RESP_WILL_DEFEND_DIVORCE, NO_VALUE);

        CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TestConstants.TEST_CASE_ID)
            .caseData(caseData)
            .build();

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        aosSubmissionWorkflow.run(ccdCallbackRequest, AUTH_TOKEN);

        ArgumentCaptor<TaskContext> argument = ArgumentCaptor.forClass(TaskContext.class);
        verify(emailNotificationTask).execute(argument.capture(), eq(ccdCallbackRequest.getCaseDetails().getCaseData()));

        TaskContext capturedTask = argument.getValue();

        DefaultTaskContext expectedContext = createdExpectedContext(EmailTemplateNames.SOL_APPLICANT_AOS_RECEIVED, true);

        assertThat(expectedContext, equalTo(capturedTask));
    }

    private DefaultTaskContext createdExpectedContext(EmailTemplateNames template, boolean isSolicitor) {

        Map<String, Object> expectedTemplateVars = new HashMap<>();
        DefaultTaskContext expectedContext = new DefaultTaskContext();
        if (isSolicitor) {
            expectedTemplateVars.put(NOTIFICATION_EMAIL, TestConstants.TEST_USER_EMAIL);
            expectedTemplateVars.put(NOTIFICATION_PET_NAME, TestConstants.TEST_PETITIONER_FIRST_NAME + " " + TestConstants.TEST_PETITIONER_LAST_NAME);
            expectedTemplateVars.put(NOTIFICATION_RESP_NAME, TestConstants.TEST_USER_FIRST_NAME + " " + TestConstants.TEST_USER_LAST_NAME);
            expectedTemplateVars.put(NOTIFICATION_SOLICITOR_NAME, TestConstants.TEST_SOLICITOR_NAME);
            expectedTemplateVars.put(NOTIFICATION_CCD_REFERENCE_KEY, TestConstants.TEST_CASE_ID);

            expectedContext.setTransientObjects(ImmutableMap
                .of(CASE_ID_JSON_KEY, TestConstants.TEST_CASE_ID,
                    NOTIFICATION_EMAIL, TestConstants.TEST_USER_EMAIL,
                    NOTIFICATION_TEMPLATE_VARS, expectedTemplateVars,
                    NOTIFICATION_TEMPLATE, template
                ));
        } else {
            expectedTemplateVars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, TestConstants.TEST_USER_FIRST_NAME);
            expectedTemplateVars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, TestConstants.TEST_USER_LAST_NAME);
            expectedTemplateVars.put(NOTIFICATION_RELATIONSHIP_KEY, "husband");
            expectedTemplateVars.put(NOTIFICATION_WELSH_HUSBAND_OR_WIFE, TestConstants.TEST_WELSH_MALE_GENDER_IN_RELATION);
            expectedTemplateVars.put(NOTIFICATION_REFERENCE_KEY, TestConstants.TEST_CASE_FAMILY_MAN_ID);

            expectedContext.setTransientObjects(ImmutableMap
                .of(CASE_ID_JSON_KEY, TestConstants.TEST_CASE_ID,
                    NOTIFICATION_EMAIL, TestConstants.TEST_USER_EMAIL,
                    NOTIFICATION_TEMPLATE_VARS, expectedTemplateVars,
                    NOTIFICATION_TEMPLATE, template
                ));
        }
        return expectedContext;
    }
}
