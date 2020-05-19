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
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseFormatterAddDocuments;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FetchPrintDocsFromDmStore;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ModifyDueDate;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ResetRespondentLinkingFields;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RespondentLetterGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RespondentPinGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.RespondentAosPackPrinterTask;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
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
    private CaseFormatterAddDocuments caseFormatterAddDocuments;

    @Mock
    private FetchPrintDocsFromDmStore fetchPrintDocsFromDmStore;

    @Mock
    private RespondentAosPackPrinterTask respondentAosPackPrinterTask;

    @Mock
    private ModifyDueDate modifyDueDate;

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
            caseFormatterAddDocuments,
            fetchPrintDocsFromDmStore,
            respondentAosPackPrinterTask,
            modifyDueDate,
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
        when(caseFormatterAddDocuments.execute(context, payload)).thenReturn(payload);
        when(fetchPrintDocsFromDmStore.execute(context, payload)).thenReturn(payload);
        when(respondentAosPackPrinterTask.execute(context, payload)).thenReturn(payload);
        when(modifyDueDate.execute(context, payload)).thenReturn(payload);
        when(resetRespondentLinkingFields.execute(context, payload)).thenReturn(payload);

        //When
        Map<String, Object> response = respondentSolicitorNominatedWorkflow.run(caseDetails, AUTH_TOKEN);

        //Then
        InOrder inOrder = inOrder(
            respondentPinGenerator,
            respondentLetterGenerator,
            caseFormatterAddDocuments,
            fetchPrintDocsFromDmStore,
            respondentAosPackPrinterTask,
            modifyDueDate,
            resetRespondentLinkingFields);
        assertThat(response, is(payload));
        inOrder.verify(respondentPinGenerator).execute(context, payload);
        inOrder.verify(respondentLetterGenerator).execute(context, payload);
        inOrder.verify(caseFormatterAddDocuments).execute(context, payload);
        inOrder.verify(fetchPrintDocsFromDmStore).execute(context, payload);
        inOrder.verify(respondentAosPackPrinterTask).execute(context, payload);
        inOrder.verify(modifyDueDate).execute(context, payload);
        inOrder.verify(resetRespondentLinkingFields).execute(context, payload);
    }
}
