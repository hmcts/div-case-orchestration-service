package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
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
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GenericEmailNotification;

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
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CO_RESPONDENT_NAMED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_INFERRED_RESPONDENT_GENDER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RELATIONSHIP_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_TEMPLATE_VARS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_CO_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@RunWith(MockitoJUnitRunner.class)
public class RespondentSubmittedCallbackWorkflowUTest {

    @Mock
    private GenericEmailNotification emailNotificationTask;

    @InjectMocks
    private RespondentSubmittedCallbackWorkflow classToTest;

    @Test
    public void givenCaseDetail_whenRunWorkflow_thenEmailNotificationTaskCalled() throws WorkflowException {
        Map<String, String> vars = ImmutableMap.of(
                NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, TestConstants.TEST_USER_FIRST_NAME,
                NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, TestConstants.TEST_USER_LAST_NAME,
                NOTIFICATION_RELATIONSHIP_KEY, "husband",
                NOTIFICATION_REFERENCE_KEY, TestConstants.TEST_CASE_FAMILY_MAN_ID
        );

        CaseDetails caseDetails = CaseDetails.builder()
                .caseId(TestConstants.TEST_CASE_ID)
                .caseData(ImmutableMap.of(
                        D_8_PETITIONER_FIRST_NAME, TestConstants.TEST_USER_FIRST_NAME,
                        D_8_PETITIONER_LAST_NAME, TestConstants.TEST_USER_LAST_NAME,
                        D_8_PETITIONER_EMAIL, TestConstants.TEST_USER_EMAIL,
                        D_8_CASE_REFERENCE, TestConstants.TEST_CASE_FAMILY_MAN_ID,
                        D_8_INFERRED_RESPONDENT_GENDER, "male"))
                .build();
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        when(emailNotificationTask.execute(any(), any())).thenReturn(caseDetails.getCaseData());
        Map<String, Object> response = classToTest.run(ccdCallbackRequest, TestConstants.TEST_TOKEN);

        verify(emailNotificationTask, times(1))
                .execute(argThat(argument ->
                        argument.getTransientObject(ID).equals(TestConstants.TEST_CASE_ID)
                                && argument.getTransientObject(NOTIFICATION_TEMPLATE_VARS).equals(vars)),any());
        assertEquals(caseDetails.getCaseData(), response);
    }

    @Test
    public void givenCaseDetailWithNullData_whenRunWorkflow_thenEmailNotificationTaskCalledWithNullData()
            throws WorkflowException {
        Map<String, String> vars =  new HashMap<>();
        vars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, null);
        vars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, null);
        vars.put(NOTIFICATION_RELATIONSHIP_KEY,  null);
        vars.put(NOTIFICATION_REFERENCE_KEY, null);

        CaseDetails caseDetails = CaseDetails.builder()
                .caseData(ImmutableMap.of())
                .build();
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        when(emailNotificationTask.execute(any(), any())).thenReturn(caseDetails.getCaseData());
        Map<String, Object> response = classToTest.run(ccdCallbackRequest, TestConstants.TEST_TOKEN);

        verify(emailNotificationTask, times(1)).execute(argThat(
            argument -> argument.getTransientObject(NOTIFICATION_TEMPLATE_VARS).equals(vars)),any());
        assertEquals(caseDetails.getCaseData(), response);
    }

    @Test
    public void givenAdulteryCoRespNotRepliedRespUndefended_whenSendEmail_thenSendRespUndefendedCoRespNoReplyTemplate() throws Exception {
        Map<String, String> vars = ImmutableMap.of(
                NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, TestConstants.TEST_USER_FIRST_NAME,
                NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, TestConstants.TEST_USER_LAST_NAME,
                NOTIFICATION_RELATIONSHIP_KEY, "husband",
                NOTIFICATION_REFERENCE_KEY, TestConstants.TEST_CASE_FAMILY_MAN_ID
        );

        Map<String, Object> caseData = new HashMap<>();

        caseData.put(D_8_PETITIONER_FIRST_NAME, TestConstants.TEST_USER_FIRST_NAME);
        caseData.put(D_8_PETITIONER_LAST_NAME, TestConstants.TEST_USER_LAST_NAME) ;
        caseData.put(D_8_PETITIONER_EMAIL, TestConstants.TEST_USER_EMAIL);
        caseData.put(D_8_CASE_REFERENCE, TestConstants.TEST_CASE_FAMILY_MAN_ID);
        caseData.put(D_8_INFERRED_RESPONDENT_GENDER, "male");
        caseData.put(D_8_CO_RESPONDENT_NAMED, YES_VALUE);
        caseData.put(RECEIVED_AOS_FROM_CO_RESP, NO_VALUE);

        CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TestConstants.TEST_CASE_ID)
            .caseData(caseData)
            .build();

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        classToTest.run(ccdCallbackRequest, TestConstants.AUTH_TOKEN);

        ArgumentCaptor<TaskContext> argument = ArgumentCaptor.forClass(TaskContext.class);
        verify(emailNotificationTask).execute(argument.capture(), eq(ccdCallbackRequest.getCaseDetails().getCaseData()));

        TaskContext capturedTask = argument.getValue();

        DefaultTaskContext expectedContext = createdExpectedContext(EmailTemplateNames.RESPONDENT_SUBMISSION_CONSENT_CORESP_NOT_REPLIED);

        assertThat(expectedContext, equalTo(capturedTask));
    }

    @Test
    public void givenAdulteryCoRespRepliedRespUndefended_whenSendEmail_thenSendRespUndefendedTemplate() throws Exception {
        Map<String, String> vars = ImmutableMap.of(
            NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, TestConstants.TEST_USER_FIRST_NAME,
            NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, TestConstants.TEST_USER_LAST_NAME,
            NOTIFICATION_RELATIONSHIP_KEY, "husband",
            NOTIFICATION_REFERENCE_KEY, TestConstants.TEST_CASE_FAMILY_MAN_ID
        );

        Map<String, Object> caseData = new HashMap<>();

        caseData.put(D_8_PETITIONER_FIRST_NAME, TestConstants.TEST_USER_FIRST_NAME);
        caseData.put(D_8_PETITIONER_LAST_NAME, TestConstants.TEST_USER_LAST_NAME) ;
        caseData.put(D_8_PETITIONER_EMAIL, TestConstants.TEST_USER_EMAIL);
        caseData.put(D_8_CASE_REFERENCE, TestConstants.TEST_CASE_FAMILY_MAN_ID);
        caseData.put(D_8_INFERRED_RESPONDENT_GENDER, "male");
        caseData.put(D_8_CO_RESPONDENT_NAMED, YES_VALUE);
        caseData.put(RECEIVED_AOS_FROM_CO_RESP, YES_VALUE);

        CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TestConstants.TEST_CASE_ID)
            .caseData(caseData)
            .build();

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        classToTest.run(ccdCallbackRequest, TestConstants.AUTH_TOKEN);

        ArgumentCaptor<TaskContext> argument = ArgumentCaptor.forClass(TaskContext.class);
        verify(emailNotificationTask).execute(argument.capture(), eq(ccdCallbackRequest.getCaseDetails().getCaseData()));

        TaskContext capturedTask = argument.getValue();

        DefaultTaskContext expectedContext = createdExpectedContext(EmailTemplateNames.RESPONDENT_SUBMISSION_CONSENT);

        assertThat(expectedContext, equalTo(capturedTask));
    }

    private DefaultTaskContext createdExpectedContext(EmailTemplateNames template) {

        Map<String, Object> expectedTemplateVars = new HashMap<>();
        expectedTemplateVars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, TestConstants.TEST_USER_FIRST_NAME);
        expectedTemplateVars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, TestConstants.TEST_USER_LAST_NAME);
        expectedTemplateVars.put(NOTIFICATION_RELATIONSHIP_KEY, "husband");
        expectedTemplateVars.put(NOTIFICATION_REFERENCE_KEY, TestConstants.TEST_CASE_FAMILY_MAN_ID);

        DefaultTaskContext expectedContext = new DefaultTaskContext();
        expectedContext.setTransientObjects(ImmutableMap
            .of(NOTIFICATION_EMAIL, TestConstants.TEST_USER_EMAIL,
                AUTH_TOKEN_JSON_KEY, TestConstants.AUTH_TOKEN,
                NOTIFICATION_TEMPLATE, template,
                NOTIFICATION_TEMPLATE_VARS, expectedTemplateVars
            ));
        return expectedContext;
    }
}
