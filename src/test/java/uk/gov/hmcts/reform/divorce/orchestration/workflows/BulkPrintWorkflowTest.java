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
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AosPackDueDateSetterTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FetchPrintDocsFromDmStoreTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.CoRespondentAosPackPrinterTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.RespondentAosPackPrinterTask;
import uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_INCOMING_CASE_DETAILS;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.mockTasksExecution;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksCalledInOrder;

@RunWith(MockitoJUnitRunner.class)
public class BulkPrintWorkflowTest {

    @Mock
    private FetchPrintDocsFromDmStoreTask fetchPrintDocsFromDmStoreTask;

    @Mock
    private RespondentAosPackPrinterTask respondentAosPackPrinterTask;

    @Mock
    private CoRespondentAosPackPrinterTask coRespondentAosPackPrinterTask;

    @Mock
    private AosPackDueDateSetterTask aosPackDueDateSetterTask;

    @Mock
    private CaseDataUtils caseDataUtils;

    @InjectMocks
    private BulkPrintWorkflow classUnderTest;

    private CaseDetails caseDetails;

    @Before
    public void setUp() {
        caseDetails = TEST_INCOMING_CASE_DETAILS;

        mockTasksExecution(
            caseDetails.getCaseData(),
            fetchPrintDocsFromDmStoreTask,
            aosPackDueDateSetterTask,
            respondentAosPackPrinterTask,
            coRespondentAosPackPrinterTask
        );
    }

    @Test
    public void whenWorkflowRunsForAdulteryCase_WithDigitalRespSol_AndNamedCoRespondent_allTasksRun() throws WorkflowException, TaskException {
        Map<String, Object> payload = caseDetails.getCaseData();
        when(caseDataUtils.isAdulteryCaseWithNamedCoRespondent(payload)).thenReturn(true);

        Map<String, Object> response = classUnderTest.run(AUTH_TOKEN, caseDetails);
        assertThat(response, is(payload));

        verifyTasksCalledInOrder(
            payload,
            fetchPrintDocsFromDmStoreTask,
            respondentAosPackPrinterTask,
            coRespondentAosPackPrinterTask,
            aosPackDueDateSetterTask
        );
    }

    @Test
    public void whenWorkflowRunsForNonAdulteryCase_WithDigitalRespSol_allTasksRunExceptForCoRespondent() throws WorkflowException, TaskException {
        Map<String, Object> payload = caseDetails.getCaseData();
        when(caseDataUtils.isAdulteryCaseWithNamedCoRespondent(payload)).thenReturn(false);

        Map<String, Object> response = classUnderTest.run(AUTH_TOKEN, caseDetails);
        assertThat(response, is(payload));

        verifyTasksCalledInOrder(
            payload,
            fetchPrintDocsFromDmStoreTask,
            respondentAosPackPrinterTask,
            aosPackDueDateSetterTask
        );

        verifyNoInteractions(coRespondentAosPackPrinterTask);
    }

}