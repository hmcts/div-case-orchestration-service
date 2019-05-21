package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_STATEMENT_OF_TRUTH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_VALIDATION_ERROR_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.STATEMENT_OF_TRUTH;

@RunWith(MockitoJUnitRunner.class)
public class ValidateSolicitorCaseDataTest {

    @InjectMocks
    private ValidateSolicitorCaseData validateSolicitorCaseData;

    private TaskContext context;
    private Map<String, Object> caseData;

    @Before
    public void setup() {
        context = new DefaultTaskContext();
        caseData = new HashMap<>();
    }

    @Test
    public void givenValidData_whenValidateSolicitorCaseData_thenReturnCaseDataWithNoErrors() {
        caseData.put(STATEMENT_OF_TRUTH, "YES");
        caseData.put(SOLICITOR_STATEMENT_OF_TRUTH, "YES");

        Map<String, Object> result = validateSolicitorCaseData.execute(context, caseData);

        assertEquals(result, caseData);
        assertEquals(false, context.hasTaskFailed());
    }

    @Test
    public void givenInvalidStatementOfTruth_whenValidateSolicitorCaseData_thenReturnCaseDataWithErrors() {
        caseData.put(STATEMENT_OF_TRUTH, "NO");
        caseData.put(SOLICITOR_STATEMENT_OF_TRUTH, "YES");

        Map<String, Object> result = validateSolicitorCaseData.execute(context, caseData);

        assertEquals(result, caseData);
        assertEquals(true, context.hasTaskFailed());
        assertNotNull(context.getTransientObject(SOLICITOR_VALIDATION_ERROR_KEY));
    }

    @Test
    public void givenInvalidSolicitorStatementOfTruth_whenValidateSolicitorCaseData_thenReturnCaseDataWithErrors() {
        caseData.put(STATEMENT_OF_TRUTH, "YES");
        caseData.put(SOLICITOR_STATEMENT_OF_TRUTH, "NO");

        Map<String, Object> result = validateSolicitorCaseData.execute(context, caseData);

        assertEquals(result, caseData);
        assertEquals(true, context.hasTaskFailed());
        assertNotNull(context.getTransientObject(SOLICITOR_VALIDATION_ERROR_KEY));
    }

    @Test
    public void givenInvalidStatementsOfTruth_whenValidateSolicitorCaseData_thenReturnCaseDataWithErrors() {
        caseData.put(STATEMENT_OF_TRUTH, "NO");
        caseData.put(SOLICITOR_STATEMENT_OF_TRUTH, "NO");

        Map<String, Object> result = validateSolicitorCaseData.execute(context, caseData);

        assertEquals(result, caseData);
        assertEquals(true, context.hasTaskFailed());
        assertNotNull(context.getTransientObject(SOLICITOR_VALIDATION_ERROR_KEY));
    }
}
