package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_PBA_PAYMENT_ERROR_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_STATEMENT_OF_TRUTH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.STATEMENT_OF_TRUTH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.context;

@RunWith(MockitoJUnitRunner.class)
public class ValidateSolicitorCaseDataTaskTest {

    @InjectMocks
    private ValidateSolicitorCaseDataTask validateSolicitorCaseDataTask;

    private TaskContext taskContext;
    private Map<String, Object> caseData;

    @Before
    public void setup() {
        taskContext = context();
        caseData = new HashMap<>();
    }

    @Test
    public void givenValidData_whenValidateSolicitorCaseData_thenReturnCaseDataWithNoErrors() {
        runTest(YES_VALUE, YES_VALUE);
        assertFalse(taskContext.hasTaskFailed());
    }

    @Test
    public void givenInvalidStatementOfTruth_whenValidateSolicitorCaseData_thenReturnCaseDataWithErrors() {
        runTestExpectErrors(NO_VALUE, YES_VALUE);
    }

    @Test
    public void givenInvalidSolicitorStatementOfTruth_whenValidateSolicitorCaseData_thenReturnCaseDataWithErrors() {
        runTestExpectErrors(YES_VALUE, NO_VALUE);
    }

    @Test
    public void givenInvalidStatementsOfTruth_whenValidateSolicitorCaseData_thenReturnCaseDataWithErrors() {
        runTestExpectErrors(NO_VALUE, NO_VALUE);
    }

    private void runTestExpectErrors(String noValue, String yesValue) {
        runTest(noValue, yesValue);

        assertTrue(taskContext.hasTaskFailed());
        assertNotNull(taskContext.getTransientObject(SOLICITOR_PBA_PAYMENT_ERROR_KEY));
    }

    private void runTest(String noValue, String yesValue) {
        caseData.put(STATEMENT_OF_TRUTH, noValue);
        caseData.put(SOLICITOR_STATEMENT_OF_TRUTH, yesValue);

        Map<String, Object> result = validateSolicitorCaseDataTask.execute(taskContext, caseData);

        assertEquals(result, caseData);
    }
}
