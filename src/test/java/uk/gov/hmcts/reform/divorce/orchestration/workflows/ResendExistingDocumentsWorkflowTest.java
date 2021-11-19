package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FetchPrintDocsFromDmStoreTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.ResendExistingDocumentsPrinterTask;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.mockTasksExecution;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksCalledInOrder;

@RunWith(MockitoJUnitRunner.class)
public class ResendExistingDocumentsWorkflowTest {

    @Mock
    private FetchPrintDocsFromDmStoreTask fetchPrintDocsFromDmStoreTask;

    @Mock
    private ResendExistingDocumentsPrinterTask resendExistingDocumentsTask;

    @InjectMocks
    private ResendExistingDocumentsWorkflow classUnderTest;

    private Map<String, Object> payload;

    private CaseDetails caseDetails;

    @Before
    public void setUp() {
        payload = new HashMap<>();
        // payload.put(RESP_SOL_REPRESENTED, YES_VALUE);

        caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .caseData(payload)
            .build();

        mockTaskExecution();
    }

    @Test
    public void shouldCallAllTasksWhenAppropriate() throws WorkflowException, TaskException {
        Map<String, Object> response = classUnderTest.run(caseDetails);
        assertThat(response, is(payload));

        verifyTasksCalledInOrder(
            payload,
            resendExistingDocumentsTask
        );
    }

    private void mockTaskExecution() {
        mockTasksExecution(
            payload,
            resendExistingDocumentsTask
        );
    }
}
