package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddNewDocumentsToCaseDataTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AosPackDueDateSetterTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FetchPrintDocsFromDmStoreTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ResetRespondentLinkingFields;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RespondentLetterGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RespondentPinGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.RespondentAosPackPrinterTask;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class RespondentSolicitorNominatedWorkflowTest {

    private RespondentSolicitorNominatedWorkflow respondentSolicitorNominatedWorkflow;

    @Mock
    private RespondentPinGenerator respondentPinGenerator;

    @Mock
    private RespondentLetterGenerator respondentLetterGenerator;

    @Mock
    private AddNewDocumentsToCaseDataTask addNewDocumentsToCaseDataTask;

    @Mock
    private FetchPrintDocsFromDmStoreTask fetchPrintDocsFromDmStoreTask;

    @Mock
    private RespondentAosPackPrinterTask respondentAosPackPrinterTask;

    @Mock
    private AosPackDueDateSetterTask aosPackDueDateSetterTask;

    @Mock
    private ResetRespondentLinkingFields resetRespondentLinkingFields;

    private CaseDetails caseDetails;
    private Map<String, Object> payload;
    private TaskContext context;

    @Before
    public void setUp() {
        respondentSolicitorNominatedWorkflow = new RespondentSolicitorNominatedWorkflow(
            respondentPinGenerator,
            respondentLetterGenerator,
            addNewDocumentsToCaseDataTask,
            fetchPrintDocsFromDmStoreTask,
            respondentAosPackPrinterTask,
            aosPackDueDateSetterTask,
            resetRespondentLinkingFields
        );

        payload = new HashMap<>();

        caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .caseData(payload)
            .build();

        context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);
    }

    @Test
    public void testRunCallsTheRequiredTasks() throws WorkflowException {

        //Given
        when(respondentPinGenerator.execute(context, payload)).thenReturn(payload);
        when(respondentLetterGenerator.execute(context, payload)).thenReturn(payload);
        when(addNewDocumentsToCaseDataTask.execute(context, payload)).thenReturn(payload);
        when(fetchPrintDocsFromDmStoreTask.execute(context, payload)).thenReturn(payload);
        when(respondentAosPackPrinterTask.execute(context, payload)).thenReturn(payload);
        when(aosPackDueDateSetterTask.execute(context, payload)).thenReturn(payload);
        when(resetRespondentLinkingFields.execute(context, payload)).thenReturn(payload);

        //When
        Map<String, Object> response = respondentSolicitorNominatedWorkflow.run(caseDetails, AUTH_TOKEN);

        //Then
        InOrder inOrder = inOrder(
            respondentPinGenerator,
            respondentLetterGenerator,
            addNewDocumentsToCaseDataTask,
            fetchPrintDocsFromDmStoreTask,
            respondentAosPackPrinterTask,
            aosPackDueDateSetterTask,
            resetRespondentLinkingFields);
        assertThat(response, is(payload));
        inOrder.verify(respondentPinGenerator).execute(context, payload);
        inOrder.verify(respondentLetterGenerator).execute(context, payload);
        inOrder.verify(addNewDocumentsToCaseDataTask).execute(context, payload);
        inOrder.verify(fetchPrintDocsFromDmStoreTask).execute(context, payload);
        inOrder.verify(respondentAosPackPrinterTask).execute(context, payload);
        inOrder.verify(aosPackDueDateSetterTask).execute(context, payload);
        inOrder.verify(resetRespondentLinkingFields).execute(context, payload);
    }
}
