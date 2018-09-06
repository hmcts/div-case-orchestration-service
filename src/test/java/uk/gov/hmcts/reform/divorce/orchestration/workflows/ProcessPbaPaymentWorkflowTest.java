package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CreateEvent;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.FeeResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.OrderSummary;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GetPetitionIssueFee;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ProcessPbaPayment;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetOrderSummary;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ValidateSolicitorCaseData;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_AMOUNT;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_DESCRIPTION;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_VERSION;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PIN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PIN;

@RunWith(MockitoJUnitRunner.class)
public class ProcessPbaPaymentWorkflowTest {

    @Mock
    ValidateSolicitorCaseData validateSolicitorCaseData;

    @Mock
    ProcessPbaPayment processPbaPayment;

    @InjectMocks
    ProcessPbaPaymentWorkflow processPbaPaymentWorkflow;

    private CaseDetails caseDetails;
    private CreateEvent createEventRequest;
    private Map<String, Object> testData;
    private TaskContext context;

    @Before
    public void setup() {
        testData = Collections.emptyMap();

        caseDetails = CaseDetails.builder()
                .caseId(TEST_CASE_ID)
                .state(TEST_STATE)
                .caseData(testData)
                .build();
        createEventRequest =
                CreateEvent.builder()
                        .eventId(TEST_EVENT_ID)
                        .token(TEST_TOKEN)
                        .caseDetails(
                                caseDetails
                        )
                        .build();

        context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
    }

    @Test
    public void runShouldExecuteTasksAndReturnPayload() throws Exception {
        when(validateSolicitorCaseData.execute(context, testData)).thenReturn(testData);
        when(processPbaPayment.execute(context, testData)).thenReturn(testData);

        assertEquals(testData, processPbaPaymentWorkflow.run(createEventRequest, AUTH_TOKEN));

        verify(validateSolicitorCaseData).execute(context, testData);
        verify(processPbaPayment).execute(context, testData);
    }

    @Test(expected = WorkflowException.class)
    public void runShouldThrowWorkflowExceptionWhenTaskExceptionIsThrown() throws Exception {
        when(validateSolicitorCaseData.execute(context, testData)).thenThrow(new TaskException("An Error"));

        processPbaPaymentWorkflow.run(createEventRequest, AUTH_TOKEN);

        verify(validateSolicitorCaseData).execute(context, testData);
    }
}
