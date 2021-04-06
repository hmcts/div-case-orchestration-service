package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.Court;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CopyJurisdictionConnectionPolicyTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CourtAllocationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.DeleteDraftTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.DuplicateCaseValidationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FormatDivorceSessionToCaseDataTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SubmitCaseToCCD;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateRespondentDigitalDetailsTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ValidateCaseDataTask;

import java.util.Arrays;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_SESSION_RESPONDENT_SOLICITOR_REFERENCE_DATA_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.CourtConstants.ALLOCATED_COURT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.CourtConstants.REASON_FOR_DIVORCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.mockTasksExecution;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksCalledInOrder;
import static uk.gov.hmcts.reform.divorce.orchestration.workflows.SubmitToCCDWorkflow.SELECTED_COURT;

@RunWith(MockitoJUnitRunner.class)
public class SubmitToCCDWorkflowTest {

    @Mock
    private CourtAllocationTask courtAllocationTask;

    @Mock
    private FormatDivorceSessionToCaseDataTask formatDivorceSessionToCaseDataTask;

    @Mock
    private CopyJurisdictionConnectionPolicyTask copyJurisdictionConnectionPolicyTask;

    @Mock
    private ValidateCaseDataTask validateCaseDataTask;

    @Mock
    private SubmitCaseToCCD submitCaseToCCD;

    @Mock
    private DeleteDraftTask deleteDraftTask;

    @Mock
    private DuplicateCaseValidationTask duplicateCaseValidationTask;

    @Mock
    private UpdateRespondentDigitalDetailsTask updateRespondentDigitalDetailsTask;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private SubmitToCCDWorkflow submitToCCDWorkflow;

    private static final String testCourtId = "randomlySelectedCourt";

    private Map<String, Object> incomingPayload;

    @Test
    public void runShouldExecuteTasks_AddCourtToContext_AndReturnPayloadWithAllocatedCourt() throws Exception {
        when(featureToggleService.isFeatureEnabled(Features.REPRESENTED_RESPONDENT_JOURNEY)).thenReturn(true);

        Court testCourt = new Court();
        testCourt.setCourtId(testCourtId);

        incomingPayload = Map.of(
            REASON_FOR_DIVORCE_KEY, "adultery",
            DIVORCE_SESSION_RESPONDENT_SOLICITOR_REFERENCE_DATA_ID, "123");
        when(courtAllocationTask.execute(any(), eq(incomingPayload))).thenAnswer(invocation -> {
            Arrays.stream(invocation.getArguments())
                .filter(TaskContext.class::isInstance)
                .map(TaskContext.class::cast)
                .findFirst()
                .ifPresent(cont -> cont.setTransientObject(SELECTED_COURT, testCourt));

            return incomingPayload;
        });

        mockTaskExecution();

        Map<String, Object> actual = submitToCCDWorkflow.run(incomingPayload, AUTH_TOKEN);

        assertThat(actual, hasEntry(equalTo(ALLOCATED_COURT_KEY), equalTo(testCourt)));

        verifyTasksCalledInOrder(
            incomingPayload,
            duplicateCaseValidationTask,
            courtAllocationTask,
            copyJurisdictionConnectionPolicyTask,
            formatDivorceSessionToCaseDataTask,
            validateCaseDataTask,
            updateRespondentDigitalDetailsTask,
            submitCaseToCCD,
            deleteDraftTask
        );
    }

    @Test
    public void runShouldExecuteAllTasks_ExceptUpdateRespDigital_ToggledOff() throws Exception {
        when(featureToggleService.isFeatureEnabled(Features.REPRESENTED_RESPONDENT_JOURNEY)).thenReturn(false);

        Court testCourt = new Court();
        testCourt.setCourtId(testCourtId);

        incomingPayload = Map.of(
            REASON_FOR_DIVORCE_KEY, "adultery",
            DIVORCE_SESSION_RESPONDENT_SOLICITOR_REFERENCE_DATA_ID, "123");
        when(courtAllocationTask.execute(any(), eq(incomingPayload))).thenAnswer(invocation -> {
            Arrays.stream(invocation.getArguments())
                .filter(TaskContext.class::isInstance)
                .map(TaskContext.class::cast)
                .findFirst()
                .ifPresent(cont -> cont.setTransientObject(SELECTED_COURT, testCourt));

            return incomingPayload;
        });

        mockTaskExecution();

        Map<String, Object> actual = submitToCCDWorkflow.run(incomingPayload, AUTH_TOKEN);

        assertThat(actual, hasEntry(equalTo(ALLOCATED_COURT_KEY), equalTo(testCourt)));

        verifyTasksCalledInOrder(
            incomingPayload,
            duplicateCaseValidationTask,
            courtAllocationTask,
            copyJurisdictionConnectionPolicyTask,
            formatDivorceSessionToCaseDataTask,
            validateCaseDataTask,
            submitCaseToCCD,
            deleteDraftTask
        );

        verifyNoInteractions(updateRespondentDigitalDetailsTask);
    }

    @Test
    public void runShouldExecuteAllTasks_ExceptUpdateRespDigital_RespSolNotDigital() throws Exception {
        when(featureToggleService.isFeatureEnabled(Features.REPRESENTED_RESPONDENT_JOURNEY)).thenReturn(true);

        Court testCourt = new Court();
        testCourt.setCourtId(testCourtId);

        incomingPayload = Map.of(
            REASON_FOR_DIVORCE_KEY, "adultery");
        when(courtAllocationTask.execute(any(), eq(incomingPayload))).thenAnswer(invocation -> {
            Arrays.stream(invocation.getArguments())
                .filter(TaskContext.class::isInstance)
                .map(TaskContext.class::cast)
                .findFirst()
                .ifPresent(cont -> cont.setTransientObject(SELECTED_COURT, testCourt));

            return incomingPayload;
        });

        mockTaskExecution();

        Map<String, Object> actual = submitToCCDWorkflow.run(incomingPayload, AUTH_TOKEN);

        assertThat(actual, hasEntry(equalTo(ALLOCATED_COURT_KEY), equalTo(testCourt)));

        verifyTasksCalledInOrder(
            incomingPayload,
            duplicateCaseValidationTask,
            courtAllocationTask,
            copyJurisdictionConnectionPolicyTask,
            formatDivorceSessionToCaseDataTask,
            validateCaseDataTask,
            submitCaseToCCD,
            deleteDraftTask
        );

        verifyNoInteractions(updateRespondentDigitalDetailsTask);
    }

    private void mockTaskExecution() {
        mockTasksExecution(
            incomingPayload,
            duplicateCaseValidationTask,
            copyJurisdictionConnectionPolicyTask,
            formatDivorceSessionToCaseDataTask,
            validateCaseDataTask,
            submitCaseToCCD,
            deleteDraftTask,
            updateRespondentDigitalDetailsTask
        );
    }
}