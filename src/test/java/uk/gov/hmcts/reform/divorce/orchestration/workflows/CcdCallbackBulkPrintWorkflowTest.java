package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FetchPrintDocsFromDmStore;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ModifyDueDate;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ServiceMethodValidationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.CoRespondentAosPackPrinterTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.RespondentAosPackPrinterTask;
import uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_STATE_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class CcdCallbackBulkPrintWorkflowTest {

    @Mock
    private ServiceMethodValidationTask serviceMethodValidationTask;

    @Mock
    private FetchPrintDocsFromDmStore fetchPrintDocsFromDmStore;

    @Mock
    private RespondentAosPackPrinterTask respondentAosPackPrinterTask;

    @Mock
    private CoRespondentAosPackPrinterTask coRespondentAosPackPrinterTask;

    @Mock
    private ModifyDueDate modifyDueDate;

    @Mock
    private CaseDataUtils caseDataUtils;

    @InjectMocks
    private CcdCallbackBulkPrintWorkflow ccdCallbackBulkPrintWorkflow;

    private CcdCallbackRequest ccdCallbackRequestRequest;

    private Map<String, Object> payload;

    private TaskContext context;

    @Before
    public void setUp() {
        payload = new HashMap<>();

        CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .caseData(payload)
            .build();

        ccdCallbackRequestRequest =
            CcdCallbackRequest.builder()
                .eventId(TEST_EVENT_ID)
                .token(TEST_TOKEN)
                .caseDetails(caseDetails)
                .build();

        context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(CASE_DETAILS_JSON_KEY, ccdCallbackRequestRequest.getCaseDetails());
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
        context.setTransientObject(CASE_STATE_JSON_KEY, TEST_STATE);
    }

    @Test
    public void whenWorkflowRunsForAdulteryCase_WithNamedCoRespondent_allTasksRun_payloadReturned() throws WorkflowException, TaskException {
        when(serviceMethodValidationTask.execute(context, payload)).thenReturn(payload);
        when(fetchPrintDocsFromDmStore.execute(context, payload)).thenReturn(payload);
        when(modifyDueDate.execute(context, payload)).thenReturn(payload);
        when(respondentAosPackPrinterTask.execute(context, payload)).thenReturn(payload);
        when(coRespondentAosPackPrinterTask.execute(context, payload)).thenReturn(payload);
        when(caseDataUtils.isAdulteryCaseWithNamedCoRespondent(payload)).thenReturn(true);

        Map<String, Object> response = ccdCallbackBulkPrintWorkflow.run(ccdCallbackRequestRequest, AUTH_TOKEN);
        assertThat(response, is(payload));

        final InOrder inOrder = inOrder(
            serviceMethodValidationTask,
            fetchPrintDocsFromDmStore,
            respondentAosPackPrinterTask,
            coRespondentAosPackPrinterTask,
            modifyDueDate
        );

        inOrder.verify(serviceMethodValidationTask).execute(context, payload);
        inOrder.verify(fetchPrintDocsFromDmStore).execute(context, payload);
        inOrder.verify(respondentAosPackPrinterTask).execute(context, payload);
        inOrder.verify(coRespondentAosPackPrinterTask).execute(context, payload);
        inOrder.verify(modifyDueDate).execute(context, payload);
    }

    @Test
    public void whenWorkflowRunsForNonAdulteryCase_allTasksRunExceptForCoRespondent_payloadReturned() throws WorkflowException, TaskException {
        when(serviceMethodValidationTask.execute(context, payload)).thenReturn(payload);
        when(fetchPrintDocsFromDmStore.execute(context, payload)).thenReturn(payload);
        when(modifyDueDate.execute(context, payload)).thenReturn(payload);
        when(respondentAosPackPrinterTask.execute(context, payload)).thenReturn(payload);
        when(caseDataUtils.isAdulteryCaseWithNamedCoRespondent(payload)).thenReturn(false);

        Map<String, Object> response = ccdCallbackBulkPrintWorkflow.run(ccdCallbackRequestRequest, AUTH_TOKEN);
        assertThat(response, is(payload));

        final InOrder inOrder = inOrder(
            serviceMethodValidationTask,
            fetchPrintDocsFromDmStore,
            respondentAosPackPrinterTask,
            modifyDueDate
        );
        inOrder.verify(serviceMethodValidationTask).execute(context, payload);
        inOrder.verify(fetchPrintDocsFromDmStore).execute(context, payload);
        inOrder.verify(respondentAosPackPrinterTask).execute(context, payload);
        inOrder.verify(modifyDueDate).execute(context, payload);

        verifyZeroInteractions(coRespondentAosPackPrinterTask);
    }

    @After
    public void tearDown() {
        ccdCallbackBulkPrintWorkflow = null;
    }

}