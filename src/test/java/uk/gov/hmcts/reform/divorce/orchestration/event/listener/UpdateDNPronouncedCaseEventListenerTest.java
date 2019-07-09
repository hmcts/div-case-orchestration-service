package uk.gov.hmcts.reform.divorce.orchestration.event.listener;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.event.UpdateDNPronouncedCaseEvent;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationService;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASES_ELIGIBLE_FOR_DA_PROCESSED_COUNT;


@RunWith(MockitoJUnitRunner.class)
public class UpdateDNPronouncedCaseEventListenerTest {

    private static final String TEST_AUTH_TOKEN = "testAuthToken";
    private static final String TEST_CASE_ID = "testCaseId";
    private static final int TEST_CASES_PROCESSED_COUNT = 0;
    private TaskContext context;
    private Map<String, Object> emptyCasesList;
    private UpdateDNPronouncedCaseEvent event;
    private UpdateDNPronouncedCaseEventListener classToTest;

    @Mock
    private CaseOrchestrationService caseOrchestrationService;

    @Before
    public void setup() throws CaseOrchestrationServiceException {
        classToTest = new UpdateDNPronouncedCaseEventListener();
        context = new DefaultTaskContext();
        emptyCasesList = new HashMap<>();
        event = new UpdateDNPronouncedCaseEvent(context, TEST_AUTH_TOKEN, TEST_CASE_ID);

        resetCassesProcessedCountInContext();
        when(caseOrchestrationService.makeCaseEligibleForDA(TEST_AUTH_TOKEN, TEST_CASE_ID)).thenReturn(emptyCasesList);
        setField(classToTest, "caseOrchestrationService", caseOrchestrationService);
    }

    @Test
    public void onApplicationEvent_COSMakeCaseEligibleForDACalled() throws CaseOrchestrationServiceException {
        classToTest.onApplicationEvent(event);
        verify(caseOrchestrationService, times(1)).makeCaseEligibleForDA(TEST_AUTH_TOKEN, TEST_CASE_ID);
    }

    @Test
    public void onApplicationEvent_casesProcessedCountIncrementBy1() {
        resetCassesProcessedCountInContext();
        classToTest.onApplicationEvent(event);
        assertEquals(casesProcessedCountInContext(), TEST_CASES_PROCESSED_COUNT + 1);
    }

    @Test
    public void onApplicationEvent_serviceThrowsCOSException_exceptionCaught_casesProcessesCountNotUpdated()
        throws CaseOrchestrationServiceException {
        when(caseOrchestrationService.makeCaseEligibleForDA(TEST_AUTH_TOKEN, TEST_CASE_ID))
                .thenThrow(new CaseOrchestrationServiceException(new WorkflowException("WorkflowException message")));
        classToTest.onApplicationEvent(event);

        verify(caseOrchestrationService, times(1)).makeCaseEligibleForDA(TEST_AUTH_TOKEN, TEST_CASE_ID);
        assertEquals(casesProcessedCountInContext(), TEST_CASES_PROCESSED_COUNT);
    }

    private int casesProcessedCountInContext() {
        return (int) context.getTransientObject(CASES_ELIGIBLE_FOR_DA_PROCESSED_COUNT);
    }

    private void resetCassesProcessedCountInContext() {
        context.setTransientObject(CASES_ELIGIBLE_FOR_DA_PROCESSED_COUNT, TEST_CASES_PROCESSED_COUNT);
    }

}