package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseDataResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseDataToDivorceFormatter;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RetrieveAosCase;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CHECK_CCD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CHECK_CCD;

@RunWith(MockitoJUnitRunner.class)
public class RetrieveAosCaseWorkflowUTest {
    @Mock
    private RetrieveAosCase retrieveAosCase;
    @Mock
    private CaseDataToDivorceFormatter caseDataToDivorceFormatter;

    @InjectMocks
    private RetrieveAosCaseWorkflow classUnderTest;

    @Test
    public void whenRetrieveAos_thenProcessAsExpected() throws WorkflowException {
        final ImmutablePair<String, Object> authTokenPair = new ImmutablePair<>(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        final ImmutablePair<String, Object> checkCcdPair = new ImmutablePair<>(CHECK_CCD, CHECK_CCD);

        final Task[] tasks = new Task[]{
            retrieveAosCase,
            caseDataToDivorceFormatter
        };

        final CaseDataResponse caseDataResponse = CaseDataResponse.builder().build();

        when(classUnderTest.execute(tasks, null, authTokenPair, checkCcdPair)).thenReturn(caseDataResponse);

        CaseDataResponse actual = classUnderTest.run(TEST_CHECK_CCD, AUTH_TOKEN);

        assertEquals(caseDataResponse, actual);
    }
}