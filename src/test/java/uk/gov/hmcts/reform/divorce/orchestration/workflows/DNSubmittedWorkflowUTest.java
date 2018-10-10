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
import uk.gov.hmcts.reform.divorce.orchestration.tasks.DnSubmittedEmailNotificationTask;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;

@RunWith(MockitoJUnitRunner.class)
public class DNSubmittedWorkflowUTest {

    @Mock
    private DnSubmittedEmailNotificationTask emailNotificationTask;

    @InjectMocks
    private DNSubmittedWorkflow classToTest;

    @Test
    public void givenCaseDetail_whenRunWorkflow_thenEmailNotificationTaskCalled() throws WorkflowException {

        CaseDetails caseDetails = CaseDetails.builder()
                .caseId(TestConstants.TEST_CASE_ID)
                .caseData(ImmutableMap.of(
                        D_8_PETITIONER_FIRST_NAME, TestConstants.TEST_USER_FIRST_NAME,
                        D_8_PETITIONER_LAST_NAME, TestConstants.TEST_USER_LAST_NAME,
                        D_8_PETITIONER_EMAIL, TestConstants.TEST_USER_EMAIL))
                .build();
        CreateEvent caseEvent = CreateEvent.builder().caseDetails(caseDetails).build();

        when(emailNotificationTask.execute(any(),
                eq(caseDetails.getCaseData()))).thenReturn(caseDetails.getCaseData());
        Map<String, Object> response = classToTest.run(caseEvent, TestConstants.TEST_TOKEN);

        assertEquals(caseDetails.getCaseData(), response);
    }

}
