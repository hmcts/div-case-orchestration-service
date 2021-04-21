package uk.gov.hmcts.reform.divorce.orchestration.workflows.aos;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.TestConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.TemplateConfigService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GenericEmailNotificationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.QueueAosSolicitorSubmitTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendRespondentSubmissionNotificationForDefendedDivorceEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendRespondentSubmissionNotificationForUndefendedDivorceEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.aos.AosReceivedPetitionerSolicitorEmailTask;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.microsoft.applicationinsights.core.dependencies.google.common.collect.ImmutableMap.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_FAMILY_MAN_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_INFERRED_MALE_GENDER;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RELATIONSHIP_HUSBAND;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_WELSH_MALE_GENDER_IN_RELATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_RESPONDENT_SOLICITOR_COMPANY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CO_RESPONDENT_NAMED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_INFERRED_RESPONDENT_GENDER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.EMPTY_STRING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RELATIONSHIP_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_TEMPLATE_VARS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_WELSH_HUSBAND_OR_WIFE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_CO_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_ADMIT_OR_CONSENT_TO_FACT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_SOL_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_WILL_DEFEND_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.AOS_RECEIVED_UNDEFENDED_NO_ADMIT_ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.AOS_RECEIVED_UNDEFENDED_NO_ADMIT_ADULTERY_CORESP_NOT_REPLIED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.AOS_RECEIVED_UNDEFENDED_NO_CONSENT_2_YEARS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.RESPONDENT_SUBMISSION_CONSENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.RESPONDENT_SUBMISSION_CONSENT_CORESP_NOT_REPLIED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFact.ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFact.SEPARATION_TWO_YEARS;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.mockTasksExecution;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTaskWasCalled;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksCalledInOrder;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksWereNeverCalled;

@RunWith(MockitoJUnitRunner.class)
public class AosSubmissionWorkflowTest {

    private static final String UNFORMATTED_CASE_ID = "0123456789012345";
    private static final String RESP_ACKNOWLEDGES_SERVICE_DEFENDING_DIVORCE_JSON =
        "/jsonExamples/payloads/respondentAcknowledgesServiceDefendingDivorce.json";
    private static final String RESP_ACKNOWLEDGES_SERVICE_NOT_DEFENDING_DIVORCE_JSON =
        "/jsonExamples/payloads/respondentAcknowledgesServiceNotDefendingDivorce.json";
    private static final String RESP_ACKNOWLEDGES_SERVICE_NOT_DEFENDING_NOT_ADMITTING_DIVORCE_JSON =
        "/jsonExamples/payloads/respondentAcknowledgesServiceNotDefendingNotAdmittingDivorce.json";
    private static final String UNCLEAR_ACKNOWLEDGEMENT_OF_SERVICE_JSON =
        "/jsonExamples/payloads/unclearAcknowledgementOfService.json";
    private static final String AOS_SOLICITOR_NOMINATED_JSON =
        "/jsonExamples/payloads/aosSolicitorNominated.json";
    private static final String AOS_SOLICITOR_NOMINATED_WITHOUT_FIELDS_SET_JSON =
        "/jsonExamples/payloads/aosSolicitorNominatedWithoutFieldSet.json";

    private Map<String, Object> returnedPayloadFromTask;

    @Mock
    private SendRespondentSubmissionNotificationForDefendedDivorceEmailTask defendedDivorceNotificationEmailTask;

    @Mock
    private SendRespondentSubmissionNotificationForUndefendedDivorceEmailTask undefendedDivorceNotificationEmailTask;

    @Mock
    private AosReceivedPetitionerSolicitorEmailTask aosReceivedPetitionerSolicitorEmailTask;

    @Mock
    private QueueAosSolicitorSubmitTask queueAosSolicitorSubmitTask;

    @Mock
    private GenericEmailNotificationTask emailNotificationTask;

    @Captor
    private ArgumentCaptor<TaskContext> taskContextArgumentCaptor;

    @Mock
    private TemplateConfigService templateConfigService;

    @InjectMocks
    private AosSubmissionWorkflow aosSubmissionWorkflow;

    @Before
    public void setUp() throws TaskException {
        returnedPayloadFromTask = new HashMap<>();
        when(defendedDivorceNotificationEmailTask.execute(any(), any())).thenReturn(returnedPayloadFromTask);
        when(undefendedDivorceNotificationEmailTask.execute(any(), any())).thenReturn(returnedPayloadFromTask);
        when(queueAosSolicitorSubmitTask.execute(any(), any())).thenReturn(returnedPayloadFromTask);
        when(aosReceivedPetitionerSolicitorEmailTask.execute(any(), any())).thenReturn(returnedPayloadFromTask);
    }

    @Test
    public void testDefendedTaskIsCalledWhenWorkflowIsRun() throws WorkflowException, IOException, TaskException {
        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(
            RESP_ACKNOWLEDGES_SERVICE_DEFENDING_DIVORCE_JSON, CcdCallbackRequest.class);
        Map<String, Object> caseData = ccdCallbackRequest.getCaseDetails().getCaseData();

        setupTemplateConfigServiceExpectations();

        Map<String, Object> returnedPayloadFromWorkflow = aosSubmissionWorkflow.run(ccdCallbackRequest, AUTH_TOKEN);

        verify(defendedDivorceNotificationEmailTask).execute(taskContextArgumentCaptor.capture(), same(caseData));
        verifyNoInteractions(undefendedDivorceNotificationEmailTask);

        assertThat(returnedPayloadFromWorkflow, is(sameInstance(returnedPayloadFromTask)));
        TaskContext taskContextPassedToTask = taskContextArgumentCaptor.getValue();
        String caseIdPassedToTask = taskContextPassedToTask.getTransientObject(CASE_ID_JSON_KEY);
        assertThat(caseIdPassedToTask, is(equalTo(UNFORMATTED_CASE_ID)));
    }

    @Test
    public void testUndefendedTaskIsCalled_WhenRespondentChoosesToNotDefendDivorce() throws IOException,
        WorkflowException, TaskException {

        CcdCallbackRequest callbackRequest = getJsonFromResourceFile(
            RESP_ACKNOWLEDGES_SERVICE_NOT_DEFENDING_DIVORCE_JSON, CcdCallbackRequest.class);
        Map<String, Object> caseData = callbackRequest.getCaseDetails().getCaseData();

        setupTemplateConfigServiceExpectations();
        when(emailNotificationTask.execute(any(), any())).thenReturn(caseData);

        Map<String, Object> returnedPayloadFromWorkflow = aosSubmissionWorkflow.run(callbackRequest, AUTH_TOKEN);

        verify(undefendedDivorceNotificationEmailTask).execute(taskContextArgumentCaptor.capture(), same(caseData));
        verifyNoInteractions(defendedDivorceNotificationEmailTask);

        assertThat(returnedPayloadFromWorkflow, is(sameInstance(returnedPayloadFromTask)));
        TaskContext taskContextPassedToTask = taskContextArgumentCaptor.getValue();
        String caseIdPassedToTask = taskContextPassedToTask.getTransientObject(CASE_ID_JSON_KEY);
        assertThat(caseIdPassedToTask, is(equalTo(UNFORMATTED_CASE_ID)));
    }

    @Test
    public void testUndefendedTaskIsCalled_WhenRespondentChoosesToNotDefendDivorceButNotAdmitWhatIsSaid()
        throws IOException, WorkflowException, TaskException {

        CcdCallbackRequest callbackRequest = getJsonFromResourceFile(
            RESP_ACKNOWLEDGES_SERVICE_NOT_DEFENDING_NOT_ADMITTING_DIVORCE_JSON, CcdCallbackRequest.class);
        Map<String, Object> caseData = callbackRequest.getCaseDetails().getCaseData();

        setupTemplateConfigServiceExpectations();
        when(emailNotificationTask.execute(any(), any())).thenReturn(caseData);

        Map<String, Object> returnedPayloadFromWorkflow = aosSubmissionWorkflow.run(callbackRequest, AUTH_TOKEN);

        verify(undefendedDivorceNotificationEmailTask).execute(taskContextArgumentCaptor.capture(), same(caseData));
        verifyNoInteractions(defendedDivorceNotificationEmailTask);

        assertThat(returnedPayloadFromWorkflow, is(sameInstance(returnedPayloadFromTask)));
        TaskContext taskContextPassedToTask = taskContextArgumentCaptor.getValue();
        String caseIdPassedToTask = taskContextPassedToTask.getTransientObject(CASE_ID_JSON_KEY);
        assertThat(caseIdPassedToTask, is(equalTo(UNFORMATTED_CASE_ID)));
    }

    @Test
    public void testExceptionIsThrown_IfNotPossibleToAssert_WhetherDivorceWillBeDefended() throws IOException {
        setupTemplateConfigServiceExpectations();

        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(
            UNCLEAR_ACKNOWLEDGEMENT_OF_SERVICE_JSON, CcdCallbackRequest.class);

        WorkflowException workflowException = assertThrows(
            WorkflowException.class,
            () -> aosSubmissionWorkflow.run(ccdCallbackRequest, AUTH_TOKEN)
        );

        assertThat(
            workflowException.getMessage(),
            startsWith(String.format("%s field doesn't contain a valid value", RESP_WILL_DEFEND_DIVORCE))
        );
    }

    @Test
    public void testSolicitorTaskIsCalledWhenWorkflowIsRun_whenSolicitorIsRepresenting()
        throws WorkflowException, IOException, TaskException {

        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(
            AOS_SOLICITOR_NOMINATED_JSON, CcdCallbackRequest.class);
        Map<String, Object> caseData = ccdCallbackRequest.getCaseDetails().getCaseData();
        caseData.putAll(buildPetitionerDetail());

        setupTemplateConfigServiceExpectations();
        when(emailNotificationTask.execute(any(), any())).thenReturn(caseData);

        aosSubmissionWorkflow.run(ccdCallbackRequest, AUTH_TOKEN);

        runCommonRespondentRepresentedVerification(caseData);
    }

    @Test
    public void testSolicitorTaskIsNotCalledWhenSolicitorIsNotRepresenting() throws IOException,
        WorkflowException {
        setupTemplateConfigServiceExpectations();
        CcdCallbackRequest callbackRequest = getJsonFromResourceFile(
            RESP_ACKNOWLEDGES_SERVICE_NOT_DEFENDING_DIVORCE_JSON, CcdCallbackRequest.class);

        aosSubmissionWorkflow.run(callbackRequest, AUTH_TOKEN);

        verifyNoInteractions(queueAosSolicitorSubmitTask);
    }

    @Test
    public void testSolicitorTaskIsCalled_whenSolicitorIsRepresentingIsEmpty_andRespSolValuesExist()
        throws WorkflowException, IOException, TaskException {

        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(
            AOS_SOLICITOR_NOMINATED_WITHOUT_FIELDS_SET_JSON, CcdCallbackRequest.class);
        Map<String, Object> caseData = ccdCallbackRequest.getCaseDetails().getCaseData();
        caseData.putAll(buildPetitionerDetail());

        setupTemplateConfigServiceExpectations();
        when(emailNotificationTask.execute(any(), any())).thenReturn(caseData);

        aosSubmissionWorkflow.run(ccdCallbackRequest, AUTH_TOKEN);

        verify(queueAosSolicitorSubmitTask).execute(taskContextArgumentCaptor.capture(), same(caseData));
    }

    @Test
    public void givenCaseNotDefended_whenRunWorkflow_thenEmailNotificationTaskCalled() throws WorkflowException {
        setupTemplateConfigServiceExpectations();

        Map<String, Object> caseData = new HashMap<>();
        caseData.putAll(buildPetitionerDetail());
        caseData.put(D_8_INFERRED_RESPONDENT_GENDER, "male");
        caseData.put(RESP_WILL_DEFEND_DIVORCE, NO_VALUE);

        CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .caseData(caseData)
            .build();
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        when(emailNotificationTask.execute(any(), any())).thenReturn(caseDetails.getCaseData());

        Map<String, String> vars = ImmutableMap.of(
            NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, TEST_USER_FIRST_NAME,
            NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, TEST_USER_LAST_NAME,
            NOTIFICATION_RELATIONSHIP_KEY, TEST_RELATIONSHIP_HUSBAND,
            NOTIFICATION_REFERENCE_KEY, TEST_CASE_FAMILY_MAN_ID,
            NOTIFICATION_WELSH_HUSBAND_OR_WIFE, TestConstants.TEST_WELSH_MALE_GENDER_IN_RELATION
        );

        aosSubmissionWorkflow.run(ccdCallbackRequest, AUTH_TOKEN);

        verify(emailNotificationTask)
            .execute(argThat(argument ->
                argument.getTransientObject(CASE_ID_JSON_KEY).equals(TEST_CASE_ID)
                    && argument.getTransientObject(NOTIFICATION_TEMPLATE_VARS).equals(vars)), any());
    }

    @Test
    public void givenCaseDefended_whenRunWorkflow_thenEmailNotificationTaskNotCalled() throws WorkflowException {
        Map<String, Object> caseData = new HashMap<>();
        caseData.putAll(buildPetitionerDetail());
        caseData.put(RESP_WILL_DEFEND_DIVORCE, YES_VALUE);
        caseData.put(D_8_INFERRED_RESPONDENT_GENDER, "male");

        CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .caseData(caseData)
            .build();
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        setupTemplateConfigServiceExpectations();

        aosSubmissionWorkflow.run(ccdCallbackRequest, AUTH_TOKEN);

        verifyNoMoreInteractions(emailNotificationTask);
    }

    @Test
    public void givenCaseNoPetEmail_whenRunWorkflow_thenEmailNotificationTaskNotCalled() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.putAll(buildPetitionerDetail());
        caseData.put(D_8_INFERRED_RESPONDENT_GENDER, "male");
        caseData.put(RESP_WILL_DEFEND_DIVORCE, NO_VALUE);
        caseData.remove(D_8_PETITIONER_EMAIL);

        CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .caseData(caseData)
            .build();
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        setupTemplateConfigServiceExpectations();

        verifyNoMoreInteractions(emailNotificationTask);
    }

    @Test
    public void givenAdulteryCoRespNotRepliedRespUndefended_whenSendEmail_thenSendRespUndefendedCoRespNoReplyTemplate() throws Exception {
        Map<String, Object> caseData = new HashMap<>();
        caseData.putAll(buildPetitionerDetail());
        caseData.put(D_8_INFERRED_RESPONDENT_GENDER, "male");
        caseData.put(D_8_CO_RESPONDENT_NAMED, YES_VALUE);
        caseData.put(RECEIVED_AOS_FROM_CO_RESP, NO_VALUE);
        caseData.put(RESP_WILL_DEFEND_DIVORCE, NO_VALUE);

        CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .caseData(caseData)
            .build();

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        setupTemplateConfigServiceExpectations();

        aosSubmissionWorkflow.run(ccdCallbackRequest, AUTH_TOKEN);

        verify(emailNotificationTask).execute(taskContextArgumentCaptor.capture(), eq(ccdCallbackRequest.getCaseDetails().getCaseData()));

        TaskContext capturedTask = taskContextArgumentCaptor.getValue();

        DefaultTaskContext expectedContext = createdExpectedContext(
            RESPONDENT_SUBMISSION_CONSENT_CORESP_NOT_REPLIED,
            caseData
        );

        assertThat(expectedContext, equalTo(capturedTask));
    }

    @Test
    public void givenAdulteryCoRespNotRepliedRespNoAdmitUndefended_whenSendEmail_thenSendRespNoAdmitUndefendedCoRespNoReplyTemplate() throws Exception {
        Map<String, Object> caseData = new HashMap<>();
        caseData.putAll(buildPetitionerDetail());
        caseData.put(D_8_INFERRED_RESPONDENT_GENDER, "male");
        caseData.put(D_8_CO_RESPONDENT_NAMED, YES_VALUE);
        caseData.put(RECEIVED_AOS_FROM_CO_RESP, NO_VALUE);
        caseData.put(D_8_REASON_FOR_DIVORCE, ADULTERY.getValue());
        caseData.put(RESP_ADMIT_OR_CONSENT_TO_FACT, NO_VALUE);
        caseData.put(RESP_WILL_DEFEND_DIVORCE, NO_VALUE);

        CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .caseData(caseData)
            .build();

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        setupTemplateConfigServiceExpectations();

        aosSubmissionWorkflow.run(ccdCallbackRequest, AUTH_TOKEN);

        verify(emailNotificationTask).execute(taskContextArgumentCaptor.capture(), eq(ccdCallbackRequest.getCaseDetails().getCaseData()));

        TaskContext capturedTask = taskContextArgumentCaptor.getValue();

        DefaultTaskContext expectedContext = createdExpectedContext(
            AOS_RECEIVED_UNDEFENDED_NO_ADMIT_ADULTERY_CORESP_NOT_REPLIED,
            caseData
        );

        assertThat(expectedContext, equalTo(capturedTask));
    }

    @Test
    public void givenAdulteryRespNoAdmitUndefendedCoRespReplied_whenSendEmail_thenSendRespNoAdmitUndefendedTemplate() throws Exception {
        Map<String, Object> caseData = new HashMap<>();
        caseData.putAll(buildPetitionerDetail());
        caseData.put(D_8_INFERRED_RESPONDENT_GENDER, "male");
        caseData.put(D_8_REASON_FOR_DIVORCE, ADULTERY.getValue());
        caseData.put(RESP_ADMIT_OR_CONSENT_TO_FACT, NO_VALUE);
        caseData.put(RESP_WILL_DEFEND_DIVORCE, NO_VALUE);

        CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .caseData(caseData)
            .build();

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        setupTemplateConfigServiceExpectations();

        aosSubmissionWorkflow.run(ccdCallbackRequest, AUTH_TOKEN);

        verify(emailNotificationTask).execute(taskContextArgumentCaptor.capture(), eq(ccdCallbackRequest.getCaseDetails().getCaseData()));
        TaskContext capturedTask = taskContextArgumentCaptor.getValue();
        DefaultTaskContext expectedContext = createdExpectedContext(
            AOS_RECEIVED_UNDEFENDED_NO_ADMIT_ADULTERY,
            caseData
        );

        assertThat(expectedContext, equalTo(capturedTask));
    }

    @Test
    public void givenSep2YrRespNoConsentUndefended_whenSendEmail_thenSendRespNoConsentUndefendedTemplate() throws Exception {
        Map<String, Object> caseData = new HashMap<>();
        caseData.putAll(buildPetitionerDetail());
        caseData.put(D_8_INFERRED_RESPONDENT_GENDER, "male");
        caseData.put(D_8_REASON_FOR_DIVORCE, SEPARATION_TWO_YEARS.getValue());
        caseData.put(RESP_ADMIT_OR_CONSENT_TO_FACT, NO_VALUE);
        caseData.put(RESP_WILL_DEFEND_DIVORCE, NO_VALUE);

        CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .caseData(caseData)
            .build();

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        setupTemplateConfigServiceExpectations();

        aosSubmissionWorkflow.run(ccdCallbackRequest, AUTH_TOKEN);

        verify(emailNotificationTask).execute(taskContextArgumentCaptor.capture(), eq(ccdCallbackRequest.getCaseDetails().getCaseData()));
        TaskContext capturedTask = taskContextArgumentCaptor.getValue();
        DefaultTaskContext expectedContext = createdExpectedContext(
            AOS_RECEIVED_UNDEFENDED_NO_CONSENT_2_YEARS,
            caseData
        );

        assertThat(expectedContext, equalTo(capturedTask));
    }

    @Test
    public void givenAdulteryCoRespRepliedRespUndefended_whenSendEmail_thenSendRespUndefendedTemplate() throws Exception {
        when(templateConfigService.getRelationshipTermByGender(eq(TEST_INFERRED_MALE_GENDER), eq(LanguagePreference.ENGLISH)))
            .thenReturn(TEST_RELATIONSHIP_HUSBAND);
        when(templateConfigService.getRelationshipTermByGender(eq(TEST_INFERRED_MALE_GENDER), eq(LanguagePreference.WELSH)))
            .thenReturn(TEST_WELSH_MALE_GENDER_IN_RELATION);
        Map<String, Object> caseData = new HashMap<>();
        caseData.putAll(buildPetitionerDetail());
        caseData.put(D_8_INFERRED_RESPONDENT_GENDER, "male");
        caseData.put(D_8_CO_RESPONDENT_NAMED, YES_VALUE);
        caseData.put(RECEIVED_AOS_FROM_CO_RESP, YES_VALUE);
        caseData.put(RESP_WILL_DEFEND_DIVORCE, NO_VALUE);

        CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .caseData(caseData)
            .build();

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        aosSubmissionWorkflow.run(ccdCallbackRequest, AUTH_TOKEN);

        verify(emailNotificationTask).execute(taskContextArgumentCaptor.capture(), eq(ccdCallbackRequest.getCaseDetails().getCaseData()));

        TaskContext capturedTask = taskContextArgumentCaptor.getValue();

        DefaultTaskContext expectedContext = createdExpectedContext(
            RESPONDENT_SUBMISSION_CONSENT,
            caseData);

        assertThat(expectedContext, equalTo(capturedTask));
    }

    @Test
    public void givenCaseSolicitor_whenRunWorkflow_thenSolEmailNotificationTaskCalled() throws WorkflowException {
        Map<String, Object> caseData = new HashMap<>();
        caseData.putAll(buildPetitionerDetail());
        caseData.put(PETITIONER_SOLICITOR_EMAIL, TEST_USER_EMAIL);
        caseData.put(RESP_WILL_DEFEND_DIVORCE, NO_VALUE);
        caseData.put(D_8_INFERRED_RESPONDENT_GENDER, "male");

        CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .caseData(caseData)
            .build();

        setupTemplateConfigServiceExpectations();

        mockTasksExecution(
            caseData,
            aosReceivedPetitionerSolicitorEmailTask,
            undefendedDivorceNotificationEmailTask
        );

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        aosSubmissionWorkflow.run(ccdCallbackRequest, AUTH_TOKEN);

        verifyTasksCalledInOrder(
            caseData,
            aosReceivedPetitionerSolicitorEmailTask,
            undefendedDivorceNotificationEmailTask
        );

        verifyTasksWereNeverCalled(
            defendedDivorceNotificationEmailTask,
            emailNotificationTask,
            queueAosSolicitorSubmitTask
        );
    }

    @Test
    public void givenCaseNotDefended_whenRespondentRepresentedPetitionerNotRepresented_thenSendEmailNotification() throws IOException,
        WorkflowException {
        CcdCallbackRequest ccdCallbackRequest = setUpCommonRespondentRepresentedCallbackRequest(Collections.EMPTY_MAP);
        Map<String, Object> caseData = ccdCallbackRequest.getCaseDetails().getCaseData();

        aosSubmissionWorkflow.run(ccdCallbackRequest, AUTH_TOKEN);

        runCommonRespondentRepresentedEmailTemplateAssertion(caseData, RESPONDENT_SUBMISSION_CONSENT);
        runCommonRespondentRepresentedVerification(caseData);
    }

    @Test
    public void givenCaseNotDefended_AdulteryAndNoConsent_whenRespondentRepresentedPetitionerNotRepresented_thenSendEmailNotification()
        throws IOException, WorkflowException {
        CcdCallbackRequest ccdCallbackRequest = setUpCommonRespondentRepresentedCallbackRequest(
            of(D_8_REASON_FOR_DIVORCE, ADULTERY.getValue(),
                RESP_ADMIT_OR_CONSENT_TO_FACT, NO_VALUE,
                RECEIVED_AOS_FROM_CO_RESP, YES_VALUE)
        );
        Map<String, Object> caseData = ccdCallbackRequest.getCaseDetails().getCaseData();

        aosSubmissionWorkflow.run(ccdCallbackRequest, AUTH_TOKEN);

        runCommonRespondentRepresentedEmailTemplateAssertion(caseData, AOS_RECEIVED_UNDEFENDED_NO_ADMIT_ADULTERY);
        runCommonRespondentRepresentedVerification(caseData);
    }

    @Test
    public void givenCaseNotDefended_NoConsentCoRespNamedAndNotReplied_whenRespondentRepresentedPetitionerNotRepresented_thenSendEmailNotification()
        throws IOException, WorkflowException {
        CcdCallbackRequest ccdCallbackRequest = setUpCommonRespondentRepresentedCallbackRequest(
            of(D_8_REASON_FOR_DIVORCE, ADULTERY.getValue(),
                RESP_ADMIT_OR_CONSENT_TO_FACT, NO_VALUE,
                D_8_CO_RESPONDENT_NAMED, YES_VALUE,
                RECEIVED_AOS_FROM_CO_RESP, NO_VALUE)
        );
        Map<String, Object> caseData = ccdCallbackRequest.getCaseDetails().getCaseData();

        aosSubmissionWorkflow.run(ccdCallbackRequest, AUTH_TOKEN);

        runCommonRespondentRepresentedEmailTemplateAssertion(caseData,
            AOS_RECEIVED_UNDEFENDED_NO_ADMIT_ADULTERY_CORESP_NOT_REPLIED);
        runCommonRespondentRepresentedVerification(caseData);
    }

    @Test
    public void givenCaseNotDefended_Sep2YrAndNoConsent_whenRespondentRepresentedPetitionerNotRepresented_thenSendEmailNotification() throws
        IOException, WorkflowException {
        CcdCallbackRequest ccdCallbackRequest = setUpCommonRespondentRepresentedCallbackRequest(
            of(D_8_REASON_FOR_DIVORCE, SEPARATION_TWO_YEARS.getValue(),
                RESP_ADMIT_OR_CONSENT_TO_FACT, NO_VALUE,
                RECEIVED_AOS_FROM_CO_RESP, NO_VALUE)
        );
        Map<String, Object> caseData = ccdCallbackRequest.getCaseDetails().getCaseData();

        aosSubmissionWorkflow.run(ccdCallbackRequest, AUTH_TOKEN);

        runCommonRespondentRepresentedEmailTemplateAssertion(caseData, AOS_RECEIVED_UNDEFENDED_NO_CONSENT_2_YEARS);
        runCommonRespondentRepresentedVerification(caseData);
    }

    @Test
    public void givenCaseNotDefended_Sep2YrAndConsent_whenRespondentRepresentedPetitionerNotRepresented_thenSendEmailNotification() throws
        IOException, WorkflowException {
        CcdCallbackRequest ccdCallbackRequest = setUpCommonRespondentRepresentedCallbackRequest(
            of(D_8_REASON_FOR_DIVORCE, SEPARATION_TWO_YEARS.getValue(),
                RESP_ADMIT_OR_CONSENT_TO_FACT, YES_VALUE,
                RECEIVED_AOS_FROM_CO_RESP, NO_VALUE)
        );
        Map<String, Object> caseData = ccdCallbackRequest.getCaseDetails().getCaseData();

        aosSubmissionWorkflow.run(ccdCallbackRequest, AUTH_TOKEN);

        runCommonRespondentRepresentedEmailTemplateAssertion(caseData, RESPONDENT_SUBMISSION_CONSENT);
        runCommonRespondentRepresentedVerification(caseData);
    }

    @Test
    public void givenCaseNotDefended_CoRespNamedAndNotReplied_whenRespondentRepresentedPetitionerNotRepresented_thenSendEmailNotification() throws
        IOException, WorkflowException {
        CcdCallbackRequest ccdCallbackRequest = setUpCommonRespondentRepresentedCallbackRequest(
            of(
                D_8_REASON_FOR_DIVORCE, ADULTERY.getValue(),
                RESP_ADMIT_OR_CONSENT_TO_FACT, YES_VALUE,
                D_8_CO_RESPONDENT_NAMED, YES_VALUE,
                RECEIVED_AOS_FROM_CO_RESP, NO_VALUE
            )
        );
        Map<String, Object> caseData = ccdCallbackRequest.getCaseDetails().getCaseData();

        aosSubmissionWorkflow.run(ccdCallbackRequest, AUTH_TOKEN);

        runCommonRespondentRepresentedEmailTemplateAssertion(caseData, RESPONDENT_SUBMISSION_CONSENT_CORESP_NOT_REPLIED);
        runCommonRespondentRepresentedVerification(caseData);
    }

    @Test
    public void givenCaseNotDefended_whenRespondentRepresentedPetitionerEmailNotExist_thenShouldNotSendEmailNotification() throws
        IOException, WorkflowException {
        CcdCallbackRequest ccdCallbackRequest = setUpCommonRespondentRepresentedCallbackRequest(
            of(PETITIONER_SOLICITOR_EMAIL, TEST_PETITIONER_EMAIL)
        );

        aosSubmissionWorkflow.run(ccdCallbackRequest, AUTH_TOKEN);

        verifyNoInteractions(emailNotificationTask);
    }

    @Test
    public void givenCaseNotDefended_whenRespondentRepresentedAndPetitionerEmailExist_thenShouldSendEmailNotification() throws
        IOException, WorkflowException {
        CcdCallbackRequest ccdCallbackRequest = setUpCommonRespondentRepresentedCallbackRequest(
            of(
                PETITIONER_SOLICITOR_EMAIL, EMPTY_STRING,
                D_8_PETITIONER_EMAIL, TEST_PETITIONER_EMAIL
            )
        );
        Map<String, Object> caseData = ccdCallbackRequest.getCaseDetails().getCaseData();

        aosSubmissionWorkflow.run(ccdCallbackRequest, AUTH_TOKEN);

        verifyTaskWasCalled(caseData, emailNotificationTask);
    }

    @Test
    public void givenCaseNotDefended_whenRespondentRepresentedAndPetitionerEmailNotExist_thenShouldNotSendEmailNotification() throws
        IOException, WorkflowException {
        CcdCallbackRequest ccdCallbackRequest = setUpCommonRespondentRepresentedCallbackRequest(
            of(
                PETITIONER_SOLICITOR_EMAIL, EMPTY_STRING,
                D_8_PETITIONER_EMAIL, EMPTY_STRING
            )
        );

        aosSubmissionWorkflow.run(ccdCallbackRequest, AUTH_TOKEN);

        verifyNoInteractions(emailNotificationTask);
    }

    @Test
    public void givenCaseNotDefended_whenOnlyRespondentSolicitorDetailExists_thenSendEmailNotification() throws
        IOException, WorkflowException {
        CcdCallbackRequest ccdCallbackRequest = setUpCommonRespondentRepresentedCallbackRequest(Collections.EMPTY_MAP);
        Map<String, Object> caseData = ccdCallbackRequest.getCaseDetails().getCaseData();
        caseData.remove(RESP_SOL_REPRESENTED);

        aosSubmissionWorkflow.run(ccdCallbackRequest, AUTH_TOKEN);

        runCommonRespondentRepresentedEmailTemplateAssertion(caseData, RESPONDENT_SUBMISSION_CONSENT);
        runCommonRespondentRepresentedVerification(caseData);
    }

    @Test
    public void givenCaseNotDefended_whenRespondentNotRepresented_thenNotQueueAosSolicitorSubmitTask() throws
        IOException, WorkflowException {
        CcdCallbackRequest ccdCallbackRequest = setUpCommonRespondentRepresentedCallbackRequest(Collections.EMPTY_MAP);
        Map<String, Object> caseData = ccdCallbackRequest.getCaseDetails().getCaseData();
        caseData.remove(RESP_SOL_REPRESENTED);
        caseData.remove(D8_RESPONDENT_SOLICITOR_COMPANY);

        aosSubmissionWorkflow.run(ccdCallbackRequest, AUTH_TOKEN);

        verifyNoInteractions(queueAosSolicitorSubmitTask);
    }

    private CcdCallbackRequest setUpCommonRespondentRepresentedCallbackRequest(Map<String, Object> additionalData) throws IOException {
        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(
            AOS_SOLICITOR_NOMINATED_JSON, CcdCallbackRequest.class);
        Map<String, Object> caseData = ccdCallbackRequest.getCaseDetails().getCaseData();
        caseData.putAll(buildPetitionerDetail());
        caseData.putAll(additionalData);

        setupTemplateConfigServiceExpectations();
        when(emailNotificationTask.execute(any(), any())).thenReturn(caseData);

        return ccdCallbackRequest;
    }

    private void runCommonRespondentRepresentedEmailTemplateAssertion(Map<String, Object> caseData, EmailTemplateNames expectedEmailTemplateName) {
        verify(emailNotificationTask).execute(taskContextArgumentCaptor.capture(), eq(caseData));
        TaskContext capturedTask = taskContextArgumentCaptor.getValue();

        EmailTemplateNames actualEmailTemplateNameUsed = capturedTask.getTransientObject(NOTIFICATION_TEMPLATE);
        assertThat(expectedEmailTemplateName, is(actualEmailTemplateNameUsed));
    }

    private void runCommonRespondentRepresentedVerification(Map<String, Object> caseData) {
        verifyTasksCalledInOrder(
            caseData,
            emailNotificationTask,
            queueAosSolicitorSubmitTask
        );

        verifyTasksWereNeverCalled(
            aosReceivedPetitionerSolicitorEmailTask,
            undefendedDivorceNotificationEmailTask,
            defendedDivorceNotificationEmailTask
        );
    }

    private DefaultTaskContext createdExpectedContext(EmailTemplateNames template, Map<String, Object> caseData) {
        final Map<String, Object> expectedTemplateVars = new HashMap<>();
        final DefaultTaskContext expectedContext = new DefaultTaskContext();

        expectedTemplateVars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, TEST_USER_FIRST_NAME);
        expectedTemplateVars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, TEST_USER_LAST_NAME);
        expectedTemplateVars.put(NOTIFICATION_RELATIONSHIP_KEY, TEST_RELATIONSHIP_HUSBAND);
        expectedTemplateVars.put(NOTIFICATION_WELSH_HUSBAND_OR_WIFE, TEST_WELSH_MALE_GENDER_IN_RELATION);
        expectedTemplateVars.put(NOTIFICATION_REFERENCE_KEY, TEST_CASE_FAMILY_MAN_ID);

        expectedContext.setTransientObjects(ImmutableMap
            .of(CASE_ID_JSON_KEY, TestConstants.TEST_CASE_ID,
                NOTIFICATION_EMAIL, TestConstants.TEST_USER_EMAIL,
                NOTIFICATION_TEMPLATE_VARS, expectedTemplateVars,
                NOTIFICATION_TEMPLATE, template
            ));

        return expectedContext;
    }

    private Map<String, Object> buildPetitionerDetail() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(D_8_PETITIONER_FIRST_NAME, TEST_USER_FIRST_NAME);
        caseData.put(D_8_PETITIONER_LAST_NAME, TEST_USER_LAST_NAME);
        caseData.put(D_8_PETITIONER_EMAIL, TEST_USER_EMAIL);
        caseData.put(D_8_CASE_REFERENCE, TEST_CASE_FAMILY_MAN_ID);
        return caseData;
    }

    private void setupTemplateConfigServiceExpectations() {
        when(templateConfigService.getRelationshipTermByGender(eq(TEST_INFERRED_MALE_GENDER), eq(LanguagePreference.ENGLISH)))
            .thenReturn(TEST_RELATIONSHIP_HUSBAND);
        when(templateConfigService.getRelationshipTermByGender(eq(TEST_INFERRED_MALE_GENDER), eq(LanguagePreference.WELSH)))
            .thenReturn(TEST_WELSH_MALE_GENDER_IN_RELATION);
    }
}
