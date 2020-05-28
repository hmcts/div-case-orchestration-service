package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.BO_WELSH_RESPONSE_AWAITING_REVIEW;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WELSH_PREVIOUS_STATE;

@RunWith(MockitoJUnitRunner.class)
public class WelshSetPreviousStateTaskTest {


    @Mock
    private CaseMaintenanceClient caseMaintenanceClient;
    @InjectMocks
    private WelshSetPreviousStateTask welshSetPreviousStateTask;

    private TaskContext context;
    private Map<String, Object> caseData;

    @Before
    public void setup() {
        context = new DefaultTaskContext();
        caseData = new HashMap<>();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, "KEY");
        context.setTransientObject(CASE_ID_JSON_KEY, "CASEID");
        Set<String> ignoreStates = new HashSet<>();
        ignoreStates.add("WelshDNisRefused");
        ignoreStates.add("WelshResponseAwaitingReview");
        ReflectionTestUtils.setField(welshSetPreviousStateTask, "ignoreStates", ignoreStates);
    }

    @Test
    public void testExecuteSuccess() throws TaskException {
       CaseDetails caseDetails = CaseDetails.builder().caseData(caseData).state("previous").build();
        when(caseMaintenanceClient.retrievePetitionById(context.getTransientObject(AUTH_TOKEN_JSON_KEY),
            context.getTransientObject(CASE_ID_JSON_KEY))).thenReturn(caseDetails);

        welshSetPreviousStateTask.execute(context, caseData);

        assertThat(caseData).containsEntry(WELSH_PREVIOUS_STATE, "previous");
    }

    @Test
    public void testNextStateNotSet() throws TaskException {
        CaseDetails caseDetails = CaseDetails.builder().caseData(caseData).state(BO_WELSH_RESPONSE_AWAITING_REVIEW).build();
        when(caseMaintenanceClient.retrievePetitionById(context.getTransientObject(AUTH_TOKEN_JSON_KEY),
            context.getTransientObject(CASE_ID_JSON_KEY))).thenReturn(caseDetails);

        welshSetPreviousStateTask.execute(context, caseData);

        assertThat(caseData).doesNotContainKeys(WELSH_PREVIOUS_STATE);
    }

}