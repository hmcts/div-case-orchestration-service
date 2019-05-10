package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseFormatterAddDocuments;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CoRespondentAnswersGenerator;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class GenerateCoRespondentAnswersWorkflowTest {
    @Mock
    private CoRespondentAnswersGenerator coRespondentAnswersGenerator;

    @Mock
    private CaseFormatterAddDocuments caseFormatterAddDocuments;

    @InjectMocks
    private GenerateCoRespondentAnswersWorkflow classUnderTest;

    @Test
    public void whenGetCase_thenProcessAsExpected() throws WorkflowException, TaskException {
        final DefaultTaskContext context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);

        Map<String, Object> payload = new HashMap<>();

        when(coRespondentAnswersGenerator.execute(context, payload)).thenReturn(payload);
        when(caseFormatterAddDocuments.execute(context, payload)).thenReturn(payload);

        CaseDetails caseDetails = CaseDetails.builder().caseId(TEST_CASE_ID).caseData(payload).build();

        Map<String, Object> actual = classUnderTest.run(caseDetails, AUTH_TOKEN);

        assertEquals(payload, actual);
        verify(coRespondentAnswersGenerator).execute(context, payload);
        verify(caseFormatterAddDocuments).execute(context, payload);
    }

    @Test(expected = WorkflowException.class)
    public void givenTaskException_whenGetCase_thenThrowWorkflowException() throws WorkflowException, TaskException {
        final DefaultTaskContext context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);

        Map<String, Object> payload = new HashMap<>();

        when(coRespondentAnswersGenerator.execute(context, payload)).thenThrow(TaskException.class);

        CaseDetails caseDetails = CaseDetails.builder().caseId(TEST_CASE_ID).caseData(payload).build();

        classUnderTest.run(caseDetails, AUTH_TOKEN);
        verify(coRespondentAnswersGenerator).execute(context, payload);
    }
}
