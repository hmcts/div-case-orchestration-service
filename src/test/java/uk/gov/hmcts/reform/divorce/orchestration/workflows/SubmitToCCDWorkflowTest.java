package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.Court;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CourtAllocationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.DeleteDraftTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.DuplicateCaseValidationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FormatDivorceSessionToCaseDataTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SubmitCaseToCCD;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ValidateCaseDataTask;

import java.util.Arrays;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.CourtConstants.ALLOCATED_COURT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.CourtConstants.REASON_FOR_DIVORCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.workflows.SubmitToCCDWorkflow.SELECTED_COURT;

@RunWith(MockitoJUnitRunner.class)
public class SubmitToCCDWorkflowTest {

    @Mock
    private CourtAllocationTask courtAllocationTask;

    @Mock
    private FormatDivorceSessionToCaseDataTask formatDivorceSessionToCaseDataTask;

    @Mock
    private ValidateCaseDataTask validateCaseDataTask;

    @Mock
    private SubmitCaseToCCD submitCaseToCCD;

    @Mock
    private DeleteDraftTask deleteDraftTask;

    @Mock
    private DuplicateCaseValidationTask duplicateCaseValidationTask;

    @InjectMocks
    private SubmitToCCDWorkflow submitToCCDWorkflow;

    private static final String testCourtId = "randomlySelectedCourt";

    @Test
    public void runShouldExecuteTasks_AddCourtToContext_AndReturnPayloadWithAllocatedCourt() throws Exception {
        Court testCourt = new Court();
        testCourt.setCourtId(testCourtId);

        Map<String, Object> incomingPayload = singletonMap(REASON_FOR_DIVORCE_KEY, "adultery");
        when(courtAllocationTask.execute(any(), eq(incomingPayload))).thenAnswer(invocation -> {
            Arrays.stream(invocation.getArguments())
                .filter(TaskContext.class::isInstance)
                .map(TaskContext.class::cast)
                .findFirst()
                .ifPresent(cont -> cont.setTransientObject(SELECTED_COURT, testCourt));

            return incomingPayload;
        });
        when(duplicateCaseValidationTask.execute(any(), eq(incomingPayload))).thenReturn(incomingPayload);
        when(formatDivorceSessionToCaseDataTask.execute(any(), eq(incomingPayload))).thenReturn(incomingPayload);
        when(validateCaseDataTask.execute(any(), eq(incomingPayload))).thenReturn(incomingPayload);
        when(submitCaseToCCD.execute(any(), eq(incomingPayload))).thenReturn(incomingPayload);
        when(deleteDraftTask.execute(any(), eq(incomingPayload))).thenReturn(singletonMap("Hello", "World"));

        Map<String, Object> actual = submitToCCDWorkflow.run(incomingPayload, AUTH_TOKEN);

        assertThat(actual, hasEntry(equalTo("Hello"), equalTo("World")));
        assertThat(actual, hasEntry(equalTo(ALLOCATED_COURT_KEY), equalTo(testCourt)));
        verify(duplicateCaseValidationTask).execute(argThat(isContextContainingCourtInfo()), eq(incomingPayload));
        verify(courtAllocationTask).execute(argThat(isContextContainingCourtInfo()), eq(incomingPayload));
        verify(formatDivorceSessionToCaseDataTask).execute(argThat(isContextContainingCourtInfo()), eq(incomingPayload));
        verify(validateCaseDataTask).execute(argThat(isContextContainingCourtInfo()), eq(incomingPayload));
        verify(submitCaseToCCD).execute(argThat(isContextContainingCourtInfo()), eq(incomingPayload));
        verify(deleteDraftTask).execute(argThat(isContextContainingCourtInfo()), eq(incomingPayload));
    }

    private static ArgumentMatcher<TaskContext> isContextContainingCourtInfo() {
        return cxt -> {
            Court selectedCourt = cxt.getTransientObject(SELECTED_COURT);
            return testCourtId.equals(selectedCourt.getCourtId());
        };
    }

}