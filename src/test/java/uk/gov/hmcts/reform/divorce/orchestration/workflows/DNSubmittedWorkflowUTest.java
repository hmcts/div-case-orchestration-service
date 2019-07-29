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
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseFormatterAddDocuments;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.DecreeNisiAnswersGeneratorTask;
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
    @Mock
    private DecreeNisiAnswersGeneratorTask decreeNisiAnswersGenerator;
    @Mock
    private CaseFormatterAddDocuments caseFormatterAddDocuments;

    @InjectMocks
    private DNSubmittedWorkflow classToTest;

    @Test
    public void givenCaseDetail_whenRunWorkflow_thenEmailNotificationTaskCalled() throws WorkflowException, TaskException {

        CaseDetails caseDetails = CaseDetails.builder()
                .caseId(TestConstants.TEST_CASE_ID)
                .caseData(ImmutableMap.of(
                        D_8_PETITIONER_FIRST_NAME, TestConstants.TEST_USER_FIRST_NAME,
                        D_8_PETITIONER_LAST_NAME, TestConstants.TEST_USER_LAST_NAME,
                        D_8_PETITIONER_EMAIL, TestConstants.TEST_USER_EMAIL))
                .build();
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        when(emailNotificationTask.execute(any(),
                eq(caseDetails.getCaseData()))).thenReturn(caseDetails.getCaseData());
        when(decreeNisiAnswersGenerator.execute(any(), any())).thenReturn(caseDetails.getCaseData());
        when(caseFormatterAddDocuments.execute(any(), any())).thenReturn(caseDetails.getCaseData());

        Map<String, Object> response = classToTest.run(ccdCallbackRequest, TestConstants.TEST_TOKEN);

        assertEquals(caseDetails.getCaseData(), response);
    }

}
