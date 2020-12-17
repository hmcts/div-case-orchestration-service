package uk.gov.hmcts.reform.divorce.orchestration.workflows.generalreferral;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral.GeneralApplicationAddedDateTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral.GeneralReferralSetPreviousCaseStateTask;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.mockTasksExecution;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksCalledInOrder;

@RunWith(MockitoJUnitRunner.class)
public class GeneralReferralWorkflowTest extends TestCase {

    @Mock
    private GeneralApplicationAddedDateTask generalApplicationAddedDateTask;

    @Mock
    private GeneralReferralSetPreviousCaseStateTask generalReferralSetPreviousCaseStateTask;

    @InjectMocks
    private GeneralReferralWorkflow generalReferralWorkflow;

    @Test
    public void runShouldBeExecuted() throws Exception {
        Map<String, Object> caseData = new HashMap<>();
        mockTasksExecution(caseData, generalApplicationAddedDateTask, generalReferralSetPreviousCaseStateTask);

        generalReferralWorkflow.run(CaseDetails.builder().caseData(caseData).build());

        verifyTasksCalledInOrder(
            caseData,
            generalApplicationAddedDateTask,
            generalReferralSetPreviousCaseStateTask);
    }
}
