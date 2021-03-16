package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.RequiredArgsConstructor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.FEE_PAY_BY_ACCOUNT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_HOW_TO_PAY_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOL_PAYMENT_CHEQUE_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.context;

@RunWith(MockitoJUnitRunner.class)
@RequiredArgsConstructor
public class MigrateChequeTaskTest {

    @InjectMocks
    private MigrateChequeTask migrateChequeTask;

    @Test
    public void verifyTaskUpdatesDataInCCD() throws TaskException {
        Map<String, Object> payload = new HashMap<>();
        payload.put(SOLICITOR_HOW_TO_PAY_JSON_KEY, SOL_PAYMENT_CHEQUE_VALUE);

        TaskContext context = context();
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);

        Map<String, Object> taskResponse = migrateChequeTask.execute(context, payload);

        assertEquals(taskResponse.get(SOLICITOR_HOW_TO_PAY_JSON_KEY), FEE_PAY_BY_ACCOUNT);
    }
}
