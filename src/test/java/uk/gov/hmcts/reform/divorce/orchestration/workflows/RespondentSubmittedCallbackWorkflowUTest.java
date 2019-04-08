package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.TestConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseFormatterAddDocuments;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GenericEmailNotification;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RespondentAnswersGenerator;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_INFERRED_RESPONDENT_GENDER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RELATIONSHIP_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_TEMPLATE_VARS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_WILL_DEFEND_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@RunWith(MockitoJUnitRunner.class)
public class RespondentSubmittedCallbackWorkflowUTest {

    @Mock
    private GenericEmailNotification emailNotificationTask;
    @Mock
    private RespondentAnswersGenerator respondentAnswersGenerator;
    @Mock
    private CaseFormatterAddDocuments caseFormatterAddDocuments;

    @InjectMocks
    private RespondentSubmittedCallbackWorkflow classToTest;

    @Test
    public void givenCaseNotDefended_whenRunWorkflow_thenEmailNotificationTaskCalled() throws WorkflowException {
        Map<String, String> vars = ImmutableMap.of(
                NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, TestConstants.TEST_USER_FIRST_NAME,
                NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, TestConstants.TEST_USER_LAST_NAME,
                NOTIFICATION_RELATIONSHIP_KEY, "husband",
                NOTIFICATION_REFERENCE_KEY, TestConstants.TEST_CASE_FAMILY_MAN_ID
        );
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(D_8_PETITIONER_FIRST_NAME, TestConstants.TEST_USER_FIRST_NAME);
        caseData.put(D_8_PETITIONER_LAST_NAME, TestConstants.TEST_USER_LAST_NAME);
        caseData.put(D_8_PETITIONER_EMAIL, TestConstants.TEST_USER_EMAIL);
        caseData.put(D_8_CASE_REFERENCE, TestConstants.TEST_CASE_FAMILY_MAN_ID);
        caseData.put(D_8_INFERRED_RESPONDENT_GENDER, "male");
        caseData.put(RESP_WILL_DEFEND_DIVORCE, "No");

        CaseDetails caseDetails = CaseDetails.builder()
                .caseId(TestConstants.TEST_CASE_ID)
                .caseData(caseData)
                .build();
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        when(emailNotificationTask.execute(any(), any())).thenReturn(caseDetails.getCaseData());
        when(respondentAnswersGenerator.execute(any(), any())).thenReturn(caseDetails.getCaseData());
        when(caseFormatterAddDocuments.execute(any(), any())).thenReturn(caseDetails.getCaseData());

        Map<String, Object> response = classToTest.run(ccdCallbackRequest, TestConstants.TEST_TOKEN);

        verify(emailNotificationTask, times(1))
                .execute(argThat(argument ->
                        argument.getTransientObject(ID).equals(TestConstants.TEST_CASE_ID)
                                && argument.getTransientObject(NOTIFICATION_TEMPLATE_VARS).equals(vars)), any());
        verify(respondentAnswersGenerator).execute(any(), any());
        verify(caseFormatterAddDocuments).execute(any(), any());
        assertEquals(caseDetails.getCaseData(), response);
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

        when(respondentAnswersGenerator.execute(any(), any())).thenReturn(caseDetails.getCaseData());
        when(caseFormatterAddDocuments.execute(any(), any())).thenReturn(caseDetails.getCaseData());

        Map<String, Object> response = classToTest.run(ccdCallbackRequest, TestConstants.TEST_TOKEN);

        verifyNoMoreInteractions(emailNotificationTask);
        verify(respondentAnswersGenerator).execute(any(), any());
        verify(caseFormatterAddDocuments).execute(any(), any());
        assertEquals(caseDetails.getCaseData(), response);
    }

    @Test
    public void givenCaseDetailsWithNullData_whenRunWorkflow_thenEmailNotificationTaskCalledWithNullData()
            throws WorkflowException {
        Map<String, String> vars =  new HashMap<>();
        vars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, null);
        vars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, null);
        vars.put(NOTIFICATION_RELATIONSHIP_KEY,  null);
        vars.put(NOTIFICATION_REFERENCE_KEY, null);

        CaseDetails caseDetails = CaseDetails.builder()
                .caseData(ImmutableMap.of(
                    RESP_WILL_DEFEND_DIVORCE, "No"
                ))
                .build();
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        when(emailNotificationTask.execute(any(), any())).thenReturn(caseDetails.getCaseData());
        when(respondentAnswersGenerator.execute(any(), any())).thenReturn(caseDetails.getCaseData());
        when(caseFormatterAddDocuments.execute(any(), any())).thenReturn(caseDetails.getCaseData());

        Map<String, Object> response = classToTest.run(ccdCallbackRequest, TestConstants.TEST_TOKEN);

        verify(emailNotificationTask, times(1)).execute(argThat(
            argument -> argument.getTransientObject(NOTIFICATION_TEMPLATE_VARS).equals(vars)),any());
        assertEquals(caseDetails.getCaseData(), response);
    }

}
