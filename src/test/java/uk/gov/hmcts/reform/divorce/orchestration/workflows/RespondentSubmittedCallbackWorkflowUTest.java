package uk.gov.hmcts.reform.divorce.orchestration.workflows;

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
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RespondentAnswersGenerator;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RespondentSubmittedCallbackWorkflowUTest {

    @Mock
    private RespondentAnswersGenerator respondentAnswersGenerator;
    @Mock
    private CaseFormatterAddDocuments caseFormatterAddDocuments;

    @InjectMocks
    private RespondentSubmittedCallbackWorkflow classToTest;

    @Test
    public void givenCaseData_whenRespondentSubmittedWorkflowIsCalled_thenExecuteTasks() throws WorkflowException {
        Map<String, Object> caseData = new HashMap<>();

        CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TestConstants.TEST_CASE_ID)
            .caseData(caseData)
            .build();
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        when(respondentAnswersGenerator.execute(any(), any())).thenReturn(caseDetails.getCaseData());
        when(caseFormatterAddDocuments.execute(any(), any())).thenReturn(caseDetails.getCaseData());

        Map<String, Object> response = classToTest.run(ccdCallbackRequest, TestConstants.TEST_TOKEN);
        assertEquals(caseDetails.getCaseData(), response);
    }
}
