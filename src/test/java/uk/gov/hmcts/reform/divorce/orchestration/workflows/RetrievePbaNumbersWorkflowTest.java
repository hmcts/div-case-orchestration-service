package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GetPbaNumbersTask;
import uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.mockTasksExecution;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksCalledInOrder;

@RunWith(MockitoJUnitRunner.class)
public class RetrievePbaNumbersWorkflowTest {

    @Mock
    private GetPbaNumbersTask getPbaNumbersTask;

    @InjectMocks
    private RetrievePbaNumbersWorkflow retrievePbaNumbersWorkflow;

    private CcdCallbackRequest ccdCallbackRequestRequest;
    private Map<String, Object> testData;
    private TaskContext context;

    @Before
    public void setup() {
        testData = Collections.emptyMap();

        CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .caseData(testData)
            .build();
        ccdCallbackRequestRequest =
            CcdCallbackRequest.builder()
                .eventId(TEST_EVENT_ID)
                .token(TEST_TOKEN)
                .caseDetails(
                    caseDetails
                )
                .build();

        context = TaskContextHelper.contextWithToken();
    }

    @Test
    public void runShouldExecuteTasksAndReturnPayload() throws Exception {
        mockTasksExecution(testData, getPbaNumbersTask);

        assertEquals(testData, retrievePbaNumbersWorkflow.run(ccdCallbackRequestRequest, AUTH_TOKEN));

        verifyTasksCalledInOrder(testData, getPbaNumbersTask);
    }
}
