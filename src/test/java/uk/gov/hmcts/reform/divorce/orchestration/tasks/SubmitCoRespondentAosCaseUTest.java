package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.exception.CaseNotFoundException;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.exception.ValidationException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static java.time.ZoneOffset.UTC;
import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdEvents.CO_RESP_SUBMISSION_AWAITING_ALTERNATIVE_SERVICE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdEvents.CO_RESP_SUBMISSION_AWAITING_DWP_RESPONSE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdEvents.CO_RESP_SUBMISSION_AWAITING_PROCESS_SERVER_SERVICE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_AWAITING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_AWAITING_SOLICITOR;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_COMPLETED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_OVERDUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_STARTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_SUBMITTED_AWAITING_ANSWER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_ALTERNATIVE_SERVICE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_DECREE_NISI;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_DWP_RESPONSE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_LEGAL_ADVISOR_REFERRAL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_PROCESS_SERVER_SERVICE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.DEFENDED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_DEFENDS_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_DUE_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_SUBMISSION_AOS_AWAITING_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_SUBMISSION_AOS_COMPLETED_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_SUBMISSION_AOS_DEFENDED_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_SUBMISSION_AOS_OVERDUE_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_SUBMISSION_AOS_STARTED_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_SUBMISSION_AOS_SUBMIT_AWAIT_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_SUBMISSION_AWAITING_DN_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_SUBMISSION_AWAITING_LA_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_CO_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_CO_RESP_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@RunWith(MockitoJUnitRunner.class)
public class SubmitCoRespondentAosCaseUTest {

    private final TaskContext taskContext = new DefaultTaskContext();
    private static final LocalDateTime FIXED_DATE_TIME = LocalDateTime.of(2015, 12, 25, 00, 00);

    @Mock()
    private CaseMaintenanceClient caseMaintenanceClient;

    @Mock
    private Clock clock;

    @InjectMocks
    private SubmitCoRespondentAosCase submitCoRespondentAosCase;

    @Before
    public void setup() {
        taskContext.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        taskContext.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);

        when(clock.instant()).thenReturn(FIXED_DATE_TIME.toInstant(ZoneOffset.UTC));
        when(clock.getZone()).thenReturn(UTC);
    }

    @Test
    public void willThrowExceptionIfCaseNotFound() throws TaskException {
        final Map<String, Object> submissionData = emptyMap();

        final Map<String, Object> caseUpdateResponse = new HashMap<>();
        caseUpdateResponse.put(CCD_CASE_DATA_FIELD, emptyMap());

        when(caseMaintenanceClient.retrieveAosCase(AUTH_TOKEN)).thenReturn(null);

        TaskException exception = assertThrows(TaskException.class,
            () -> submitCoRespondentAosCase.execute(taskContext, submissionData));
        assertThat(exception.getCause(), instanceOf(CaseNotFoundException.class));
        assertThat(exception.getCause().getMessage(), is("No case found for user."));

        verify(caseMaintenanceClient, never()).updateCase(anyString(), anyString(), anyString(), anyMap());
    }

    @Test
    public void willThrowExceptionIfCaseInWrongState() throws TaskException {
        final Map<String, Object> submissionData = emptyMap();

        final Map<String, Object> caseUpdateResponse = new HashMap<>();
        caseUpdateResponse.put(CCD_CASE_DATA_FIELD, emptyMap());

        when(caseMaintenanceClient.retrieveAosCase(AUTH_TOKEN)).thenReturn(someCaseWithState("foo"));

        TaskException exception = assertThrows(TaskException.class,
            () -> submitCoRespondentAosCase.execute(taskContext, submissionData));
        assertThat(exception.getCause(), instanceOf(ValidationException.class));
        assertThat(exception.getCause().getMessage(), is(String.format("Cannot create co-respondent submission event for case [%s] in state [%s].",
            TEST_CASE_ID, "foo")));

        verify(caseMaintenanceClient, never()).updateCase(anyString(), anyString(), anyString(), anyMap());
    }

    @Test
    public void givenCaseIsAosCompleted_whenCoRespondentSubmits_thenSubmitCorrectEvent() throws TaskException {
        assertForGivenStateWhenCoRespondentSubmitsThenSubmitCorrectEvent(
            AOS_COMPLETED,
            CO_RESPONDENT_SUBMISSION_AOS_COMPLETED_EVENT_ID);
    }

    @Test
    public void givenCaseIsAwaitingDN_whenCoRespondentSubmits_thenSubmitCorrectEvent() throws TaskException {
        assertForGivenStateWhenCoRespondentSubmitsThenSubmitCorrectEvent(
            AWAITING_DECREE_NISI,
            CO_RESPONDENT_SUBMISSION_AWAITING_DN_EVENT_ID);
    }

    @Test
    public void givenCaseIsAwaitingLAReferral_whenCoRespondentSubmits_thenSubmitCorrectEvent() throws TaskException {
        assertForGivenStateWhenCoRespondentSubmitsThenSubmitCorrectEvent(
            AWAITING_LEGAL_ADVISOR_REFERRAL,
            CO_RESPONDENT_SUBMISSION_AWAITING_LA_EVENT_ID);
    }

    @Test
    public void givenCaseIsAosAwaiting_whenCoRespondentSubmits_thenSubmitCorrectEvent() throws TaskException {
        assertForGivenStateWhenCoRespondentSubmitsThenSubmitCorrectEvent(
            AOS_AWAITING,
            CO_RESPONDENT_SUBMISSION_AOS_AWAITING_EVENT_ID);
    }

    @Test
    public void givenCaseIsAosAwaitingSolicitor_whenCoRespondentSubmits_thenSubmitCorrectEvent() throws TaskException {
        assertForGivenStateWhenCoRespondentSubmitsThenSubmitCorrectEvent(
            AOS_AWAITING_SOLICITOR,
            CO_RESPONDENT_SUBMISSION_AOS_AWAITING_EVENT_ID);
    }

    @Test
    public void givenCaseIsAosStarted_whenCoRespondentSubmits_thenSubmitCorrectEvent() throws TaskException {
        assertForGivenStateWhenCoRespondentSubmitsThenSubmitCorrectEvent(
            AOS_STARTED,
            CO_RESPONDENT_SUBMISSION_AOS_STARTED_EVENT_ID);
    }

    @Test
    public void givenCaseIsAosSubmittedAwaitingAnswer_whenCoRespondentSubmits_thenSubmitCorrectEvent() throws TaskException {
        assertForGivenStateWhenCoRespondentSubmitsThenSubmitCorrectEvent(
            AOS_SUBMITTED_AWAITING_ANSWER,
            CO_RESPONDENT_SUBMISSION_AOS_SUBMIT_AWAIT_EVENT_ID);
    }

    @Test
    public void givenCaseIsAosOverdue_whenCoRespondentSubmits_thenSubmitCorrectEvent() throws TaskException {
        assertForGivenStateWhenCoRespondentSubmitsThenSubmitCorrectEvent(
            AOS_OVERDUE,
            CO_RESPONDENT_SUBMISSION_AOS_OVERDUE_EVENT_ID);
    }

    @Test
    public void givenCaseIsAosDefended_whenCoRespondentSubmits_thenSubmitCorrectEvent() throws TaskException {
        assertForGivenStateWhenCoRespondentSubmitsThenSubmitCorrectEvent(
            DEFENDED,
            CO_RESPONDENT_SUBMISSION_AOS_DEFENDED_EVENT_ID);
    }

    @Test
    public void givenCaseIsAwaitingAlternativeService_whenCoRespondentSubmits_thenSubmitCorrectEvent() throws TaskException {
        assertForGivenStateWhenCoRespondentSubmitsThenSubmitCorrectEvent(
            AWAITING_ALTERNATIVE_SERVICE,
            CO_RESP_SUBMISSION_AWAITING_ALTERNATIVE_SERVICE);
    }

    @Test
    public void givenCaseIsAwaitingProcessServerService_whenCoRespondentSubmits_thenSubmitCorrectEvent() throws TaskException {
        assertForGivenStateWhenCoRespondentSubmitsThenSubmitCorrectEvent(
            AWAITING_PROCESS_SERVER_SERVICE,
            CO_RESP_SUBMISSION_AWAITING_PROCESS_SERVER_SERVICE);
    }

    @Test
    public void givenCaseIsAwaitingDWPResponse_whenCoRespondentSubmits_thenSubmitCorrectEvent() throws TaskException {
        assertForGivenStateWhenCoRespondentSubmitsThenSubmitCorrectEvent(
            AWAITING_DWP_RESPONSE,
            CO_RESP_SUBMISSION_AWAITING_DWP_RESPONSE);
    }

    @Test
    public void givenCoRespondentIsDefending_whenCoRespondentSubmits_thenDueDateIsSetTo21DaysInFuture() throws TaskException {
        final Map<String, Object> originalSubmissionData = new HashMap<>();
        originalSubmissionData.put(CO_RESPONDENT_DEFENDS_DIVORCE, "YES");
        originalSubmissionData.put(CO_RESPONDENT_DUE_DATE, "2010-01-01");


        final Map<String, Object> recalculatedSubmissionData = new HashMap<>();
        recalculatedSubmissionData.put(CO_RESPONDENT_DEFENDS_DIVORCE, "YES");
        recalculatedSubmissionData.put(CO_RESPONDENT_DUE_DATE, FIXED_DATE_TIME.plusDays(21).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        recalculatedSubmissionData.put(RECEIVED_AOS_FROM_CO_RESP, YES_VALUE);
        recalculatedSubmissionData.put(RECEIVED_AOS_FROM_CO_RESP_DATE, FIXED_DATE_TIME.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        final Map<String, Object> caseUpdateResponse = new HashMap<>();
        caseUpdateResponse.put(CCD_CASE_DATA_FIELD, emptyMap());

        when(caseMaintenanceClient.retrieveAosCase(AUTH_TOKEN)).thenReturn(someCaseWithState(DEFENDED));
        when(caseMaintenanceClient.updateCase(AUTH_TOKEN, TEST_CASE_ID, CO_RESPONDENT_SUBMISSION_AOS_DEFENDED_EVENT_ID, recalculatedSubmissionData))
            .thenReturn(caseUpdateResponse);

        assertThat(submitCoRespondentAosCase.execute(taskContext, originalSubmissionData), is(caseUpdateResponse));

        verify(caseMaintenanceClient)
            .updateCase(AUTH_TOKEN, TEST_CASE_ID, CO_RESPONDENT_SUBMISSION_AOS_DEFENDED_EVENT_ID, recalculatedSubmissionData);
    }

    @Test
    public void givenCoRespondentIsNotDefending_whenCoRespondentSubmits_thenDueDateRemainsTheSame() throws TaskException {
        final Map<String, Object> originalSubmissionData = new HashMap<>();
        originalSubmissionData.put(CO_RESPONDENT_DEFENDS_DIVORCE, "NO");
        originalSubmissionData.put(CO_RESPONDENT_DUE_DATE, "2010-01-01");


        final Map<String, Object> caseUpdateResponse = new HashMap<>();
        caseUpdateResponse.put(CCD_CASE_DATA_FIELD, emptyMap());

        when(caseMaintenanceClient.retrieveAosCase(AUTH_TOKEN)).thenReturn(someCaseWithState(DEFENDED));
        when(caseMaintenanceClient.updateCase(AUTH_TOKEN, TEST_CASE_ID, CO_RESPONDENT_SUBMISSION_AOS_DEFENDED_EVENT_ID, originalSubmissionData))
            .thenReturn(caseUpdateResponse);

        assertThat(submitCoRespondentAosCase.execute(taskContext, originalSubmissionData), is(caseUpdateResponse));

        verify(caseMaintenanceClient)
            .updateCase(AUTH_TOKEN, TEST_CASE_ID, CO_RESPONDENT_SUBMISSION_AOS_DEFENDED_EVENT_ID, originalSubmissionData);
    }

    private void assertForGivenStateWhenCoRespondentSubmitsThenSubmitCorrectEvent(String caseState, String eventToSubmit) throws TaskException {
        final Map<String, Object> submissionData = new HashMap<>();

        final Map<String, Object> caseUpdateResponse = new HashMap<>();
        caseUpdateResponse.put(CCD_CASE_DATA_FIELD, emptyMap());

        when(caseMaintenanceClient.retrieveAosCase(AUTH_TOKEN)).thenReturn(someCaseWithState(caseState));
        when(caseMaintenanceClient.updateCase(AUTH_TOKEN, TEST_CASE_ID, eventToSubmit, submissionData))
            .thenReturn(caseUpdateResponse);

        assertThat(submitCoRespondentAosCase.execute(taskContext, submissionData), is(caseUpdateResponse));

        verify(caseMaintenanceClient)
            .updateCase(AUTH_TOKEN, TEST_CASE_ID, eventToSubmit, submissionData);
    }

    private CaseDetails someCaseWithState(final String state) {
        return CaseDetails.builder().caseId(TEST_CASE_ID).state(state).build();
    }
}
