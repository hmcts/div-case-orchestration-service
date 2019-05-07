package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetCoRespondentAnswerReceived;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class CoRespondentAnswerReceivedWorkflowUTest {

    @Mock
    private SetCoRespondentAnswerReceived setCoRespondentAnswerReceived;

    @InjectMocks
    private CoRespondentAnswerReceivedWorkflow classToTest;

    @Test
    public void whenRunWorkflow_thenExecuteAllTask() throws WorkflowException {
        CaseDetails caseDetails = CaseDetails
            .builder()
            .caseData(new HashMap<>())
            .build();

        classToTest.run(caseDetails);

        verify(setCoRespondentAnswerReceived, times(1))
            .execute(any(), eq(caseDetails.getCaseData()));
    }
}
