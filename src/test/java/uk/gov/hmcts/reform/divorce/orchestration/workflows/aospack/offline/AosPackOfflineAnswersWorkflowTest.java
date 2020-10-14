package uk.gov.hmcts.reform.divorce.orchestration.workflows.aospack.offline;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddNewDocumentsToCaseDataTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RespondentAnswersGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.aospack.offline.CoRespondentAosAnswersProcessorTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.aospack.offline.CoRespondentAosDerivedAddressFormatterTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.aospack.offline.FormFieldValuesToCoreFieldsRelayTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.aospack.offline.RespondentAosAnswersProcessorTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.aospack.offline.RespondentAosDerivedAddressFormatterTask;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.model.parties.DivorceParty.CO_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.model.parties.DivorceParty.RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTaskWasNeverCalled;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksCalledInOrder;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksWereNeverCalled;

@RunWith(MockitoJUnitRunner.class)
public class AosPackOfflineAnswersWorkflowTest {

    @Mock
    private RespondentAosAnswersProcessorTask respondentAosAnswersProcessor;

    @Mock
    private CoRespondentAosAnswersProcessorTask coRespondentAosAnswersProcessor;

    @Mock
    private FormFieldValuesToCoreFieldsRelayTask formFieldValuesToCoreFieldsRelay;

    @Mock
    private CoRespondentAosDerivedAddressFormatterTask coRespondentAosDerivedAddressFormatter;

    @Mock
    private RespondentAosDerivedAddressFormatterTask respondentAosDerivedAddressFormatter;

    @Mock
    private RespondentAnswersGenerator respondentAnswersGenerator;

    @Mock
    private AddNewDocumentsToCaseDataTask addNewDocumentsToCaseDataTask;

    @InjectMocks
    private AosPackOfflineAnswersWorkflow classUnderTest;

    @Captor
    private ArgumentCaptor<TaskContext> taskContextArgumentCaptor;

    @Test
    public void shouldCallRespondentTask_ForRespondent() throws WorkflowException, TaskException {

        Map<String, Object> payload = singletonMap("testKey", "testValue");
        Map<String, Object> returnCaseData = singletonMap("returnedKey", "returnedValue");
        CaseDetails caseDetails = buildCaseDetail(payload);

        when(respondentAosAnswersProcessor.execute(any(TaskContext.class), eq(payload))).thenReturn(returnCaseData);
        when(formFieldValuesToCoreFieldsRelay.execute(any(TaskContext.class), eq(payload))).thenReturn(payload);
        when(respondentAosDerivedAddressFormatter.execute(any(TaskContext.class), anyMap())).thenReturn(returnCaseData);
        when(respondentAnswersGenerator.execute(any(TaskContext.class), anyMap())).thenReturn(returnCaseData);
        when(addNewDocumentsToCaseDataTask.execute(any(TaskContext.class), anyMap())).thenReturn(returnCaseData);

        Map<String, Object> returnedPayload = classUnderTest.run(AUTH_TOKEN, caseDetails, RESPONDENT);

        assertThat(returnedPayload, hasEntry("returnedKey", "returnedValue"));

        InOrder inOrder = inOrder(
            formFieldValuesToCoreFieldsRelay,
            respondentAosAnswersProcessor,
            respondentAosDerivedAddressFormatter,
            respondentAnswersGenerator,
            addNewDocumentsToCaseDataTask
        );
        inOrder.verify(formFieldValuesToCoreFieldsRelay).execute(any(), eq(payload));
        inOrder.verify(respondentAosAnswersProcessor).execute(any(), eq(payload));
        inOrder.verify(respondentAosDerivedAddressFormatter).execute(any(), anyMap());
        inOrder.verify(respondentAnswersGenerator).execute(taskContextArgumentCaptor.capture(), anyMap());
        inOrder.verify(addNewDocumentsToCaseDataTask).execute(any(), anyMap());
        TaskContext taskContext = taskContextArgumentCaptor.getValue();
        assertThat(taskContext.getTransientObject(AUTH_TOKEN_JSON_KEY), is(AUTH_TOKEN));
        verifyTaskWasNeverCalled(coRespondentAosAnswersProcessor);
    }

    @Test
    public void shouldCallCoRespondentTask_ForCoRespondent() throws WorkflowException, TaskException {
        Map<String, Object> payload = singletonMap("testKey", "testValue");
        Map<String, Object> returnedCaseData = singletonMap("returnedKey", "returnedValue");
        CaseDetails caseDetails = buildCaseDetail(payload);

        when(formFieldValuesToCoreFieldsRelay.execute(any(), eq(payload))).thenReturn(payload);
        when(coRespondentAosAnswersProcessor.execute(any(), eq(payload))).thenReturn(payload);
        when(coRespondentAosDerivedAddressFormatter.execute(any(), eq(payload))).thenReturn(returnedCaseData);

        Map<String, Object> returnedPayload = classUnderTest.run(AUTH_TOKEN, caseDetails, CO_RESPONDENT);

        assertThat(returnedPayload, hasEntry("returnedKey", "returnedValue"));
        verifyTasksCalledInOrder(payload, formFieldValuesToCoreFieldsRelay, coRespondentAosAnswersProcessor, coRespondentAosDerivedAddressFormatter);
        verifyTasksWereNeverCalled(respondentAosAnswersProcessor, respondentAnswersGenerator, addNewDocumentsToCaseDataTask);
    }

    @Test
    public void shouldCallCoRespondentTask_and_Update_ReceivedAosFromCoResp_to_Yes() throws WorkflowException, TaskException {
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> returnedCaseData = singletonMap("returnedKey", "returnedValue");
        CaseDetails caseDetails = buildCaseDetail(payload);

        when(coRespondentAosAnswersProcessor.execute(any(), anyMap())).thenReturn(payload);
        when(coRespondentAosDerivedAddressFormatter.execute(any(), anyMap())).thenReturn(returnedCaseData);

        Map<String, Object> returnedPayload = classUnderTest.run(AUTH_TOKEN, caseDetails, CO_RESPONDENT);

        assertThat(returnedPayload, hasEntry("returnedKey", "returnedValue"));
        verifyTasksCalledInOrder(payload, formFieldValuesToCoreFieldsRelay, coRespondentAosAnswersProcessor, coRespondentAosDerivedAddressFormatter);
        verifyTasksWereNeverCalled(respondentAosAnswersProcessor, respondentAnswersGenerator, addNewDocumentsToCaseDataTask);
    }

    private CaseDetails buildCaseDetail(Map<String, Object> payload) {
        return CaseDetails.builder().caseId(TEST_CASE_ID).caseData(payload).build();
    }

}