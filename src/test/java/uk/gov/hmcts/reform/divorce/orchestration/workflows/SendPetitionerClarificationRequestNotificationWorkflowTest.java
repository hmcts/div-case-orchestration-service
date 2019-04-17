package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendPetitionerClarificationRequestEmail;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class SendPetitionerClarificationRequestNotificationWorkflowTest {

    @Mock
    private SendPetitionerClarificationRequestEmail sendPetitionerClarificationRequestEmail;

    @InjectMocks
    private SendPetitionerClarificationRequestNotificationWorkflow classUnderTest;

    @Test
    public void willAlwaysExecuteTheEmailTask() throws WorkflowException {

        CaseDetails caseDetails = CaseDetails.builder().caseId(TEST_CASE_ID).caseData(emptyMap()).build();
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        when(sendPetitionerClarificationRequestEmail.execute(any(), any())).thenReturn(caseDetails.getCaseData());

        final Map<String, Object> response = classUnderTest.run(ccdCallbackRequest);

        verify(sendPetitionerClarificationRequestEmail)
            .execute(argThat(taskContext -> taskContext.getTransientObject(CASE_ID_JSON_KEY).equals(TEST_CASE_ID)), eq(caseDetails.getCaseData()));

        assertThat(response, is(caseDetails.getCaseData()));
    }
}
