package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.MigrateChequeTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ValidateChequePaymentTask;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.FEE_PAY_BY_ACCOUNT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_HOW_TO_PAY_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOL_PAYMENT_CHEQUE_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.context;

@RunWith(MockitoJUnitRunner.class)
public class MigrateChequePaymentWorkflowTest {

    private static final String MIGRATE_CHEQUE_PAYMENT_EVENT_ID = "MigrateChequePayment";

    @Mock
    ValidateChequePaymentTask validateChequePaymentTask;

    @Mock
    MigrateChequeTask migrateChequeTask;

    @InjectMocks
    MigrateChequePaymentWorkflow migrateChequePaymentWorkflow;

    @Test
    public void testTasksAreCalledInCorrectOrder() throws WorkflowException {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(SOLICITOR_HOW_TO_PAY_JSON_KEY, SOL_PAYMENT_CHEQUE_VALUE);

        CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .caseData(caseData)
            .build();

        CcdCallbackRequest ccdCallbackRequest = new CcdCallbackRequest();
        ccdCallbackRequest.setEventId(MIGRATE_CHEQUE_PAYMENT_EVENT_ID);
        ccdCallbackRequest.setToken(TEST_SERVICE_AUTH_TOKEN);
        ccdCallbackRequest.setCaseDetails(caseDetails);

        TaskContext context = context();
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(CASE_EVENT_ID_JSON_KEY, MIGRATE_CHEQUE_PAYMENT_EVENT_ID);
        context.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);


        Map<String, Object> migratedCaseData = new HashMap<>();
        migratedCaseData.put(SOLICITOR_HOW_TO_PAY_JSON_KEY, FEE_PAY_BY_ACCOUNT);

        when(validateChequePaymentTask.execute(context, caseData))
            .thenReturn(caseData);

        when(migrateChequeTask.execute(context, caseData))
            .thenReturn(migratedCaseData);

        Map<String, Object> returnedPayload = migrateChequePaymentWorkflow.run(ccdCallbackRequest, AUTH_TOKEN);
        assertEquals(returnedPayload.get(SOLICITOR_HOW_TO_PAY_JSON_KEY), FEE_PAY_BY_ACCOUNT);

        InOrder inOrder = inOrder(validateChequePaymentTask, migrateChequeTask);
        inOrder.verify(validateChequePaymentTask).execute(context, caseData);
        inOrder.verify(migrateChequeTask).execute(context, caseData);

    }

}