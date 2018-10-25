package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.TestConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CreateEvent;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GenericSubmittedEmailNotification;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_INFERRED_RESPONDENT_GENDER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RELATIONSHIP_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_TEMPLATE_VARS;

@RunWith(MockitoJUnitRunner.class)
public class RespondentSubmittedCallbackWorkflowUTest {

    @Mock
    private GenericSubmittedEmailNotification emailNotificationTask;

    @InjectMocks
    private RespondentSubmittedCallbackWorkflow classToTest;

    @Test
    public void givenCaseDetail_whenRunWorkflow_thenEmailNotificationTaskCalled() throws WorkflowException {
        Map<String, String> vars = ImmutableMap.of(
                NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, TestConstants.TEST_USER_FIRST_NAME,
                NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, TestConstants.TEST_USER_LAST_NAME,
                NOTIFICATION_RELATIONSHIP_KEY, "husband",
                NOTIFICATION_REFERENCE_KEY, TestConstants.TEST_CASE_ID);

        CaseDetails caseDetails = CaseDetails.builder()
                .caseId(TestConstants.TEST_CASE_ID)
                .caseData(ImmutableMap.of(
                        D_8_PETITIONER_FIRST_NAME, TestConstants.TEST_USER_FIRST_NAME,
                        D_8_PETITIONER_LAST_NAME, TestConstants.TEST_USER_LAST_NAME,
                        D_8_PETITIONER_EMAIL, TestConstants.TEST_USER_EMAIL,
                        D_8_INFERRED_RESPONDENT_GENDER, "male"))
                .build();
        CreateEvent caseEvent = CreateEvent.builder().caseDetails(caseDetails).build();

        when(emailNotificationTask.execute(any(), any())).thenReturn(caseDetails.getCaseData());
        Map<String, Object> response = classToTest.run(caseEvent, TestConstants.TEST_TOKEN);

        verify(emailNotificationTask, times(1))
                .execute(argThat(argument ->
                        argument.getTransientObject(NOTIFICATION_TEMPLATE_VARS).equals(vars)),any());
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
        CreateEvent caseEvent = CreateEvent.builder().caseDetails(caseDetails).build();

        when(emailNotificationTask.execute(any(), any())).thenReturn(caseDetails.getCaseData());
        Map<String, Object> response = classToTest.run(caseEvent, TestConstants.TEST_TOKEN);

        verify(emailNotificationTask, times(1)).execute(argThat(
            argument -> argument.getTransientObject(NOTIFICATION_TEMPLATE_VARS).equals(vars)),any());
        assertEquals(caseDetails.getCaseData(), response);
    }

}
