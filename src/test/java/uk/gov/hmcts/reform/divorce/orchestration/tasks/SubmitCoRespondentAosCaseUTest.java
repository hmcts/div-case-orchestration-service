package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_AWAITING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_COMPLETED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_OVERDUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_STARTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_SUBMITTED_AWAITING_ANSWER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_DECREE_NISI;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_LEGAL_ADVISOR_REFERRAL;
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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DEFENDED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_CO_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_CO_RESP_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@RunWith(MockitoJUnitRunner.class)
public class SubmitCoRespondentAosCaseUTest {

    private final TaskContext taskContext = new DefaultTaskContext();
    private static final LocalDateTime FIXED_DATE_TIME = LocalDateTime.of(2015, 12, 25, 00, 00);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

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

        expectedException.expect(TaskException.class);
        expectedException.expectCause(allOf(
            instanceOf(CaseNotFoundException.class),
            hasProperty("message", is("No case found for user."))));

        submitCoRespondentAosCase.execute(taskContext, submissionData);

        verify(caseMaintenanceClient, never()).updateCase(anyString(), anyString(), anyString(), anyMap());
    }

    @Test
    public void willThrowExceptionIfCaseInWrongState() throws TaskException {
        final Map<String, Object> submissionData = emptyMap();

        final Map<String, Object> caseUpdateResponse = new HashMap<>();
        caseUpdateResponse.put(CCD_CASE_DATA_FIELD, emptyMap());

        when(caseMaintenanceClient.retrieveAosCase(AUTH_TOKEN)).thenReturn(someCaseWithState("foo"));

        expectedException.expect(TaskException.class);
        expectedException.expectCause(allOf(
            instanceOf(ValidationException.class),
            hasProperty("message", is(String.format("Cannot create co-respondent submission event for case [%s] in state [%s].",
                TEST_CASE_ID, "foo")))));

        submitCoRespondentAosCase.execute(taskContext, submissionData);

        verify(caseMaintenanceClient, never()).updateCase(anyString(), anyString(), anyString(), anyMap());
    }

    @Test
    public void givenCaseIsAosCompleted_whenCoRespondentSubmits_thenSubmitCorrectEvent() throws TaskException {
        final Map<String, Object> submissionData = new HashMap<>();

        final Map<String, Object> caseUpdateResponse = new HashMap<>();
        caseUpdateResponse.put(CCD_CASE_DATA_FIELD, emptyMap());

        when(caseMaintenanceClient.retrieveAosCase(AUTH_TOKEN)).thenReturn(someCaseWithState(AOS_COMPLETED));
        when(caseMaintenanceClient.updateCase(AUTH_TOKEN, TEST_CASE_ID, CO_RESPONDENT_SUBMISSION_AOS_COMPLETED_EVENT_ID, submissionData))
            .thenReturn(caseUpdateResponse);

        assertThat(submitCoRespondentAosCase.execute(taskContext, submissionData), is(caseUpdateResponse));

        verify(caseMaintenanceClient)
            .updateCase(AUTH_TOKEN, TEST_CASE_ID, CO_RESPONDENT_SUBMISSION_AOS_COMPLETED_EVENT_ID, submissionData);
    }

    @Test
    public void givenCaseIsAwaitingDN_whenCoRespondentSubmits_thenSubmitCorrectEvent() throws TaskException {
        final Map<String, Object> submissionData = new HashMap<>();

        final Map<String, Object> caseUpdateResponse = new HashMap<>();
        caseUpdateResponse.put(CCD_CASE_DATA_FIELD, emptyMap());

        when(caseMaintenanceClient.retrieveAosCase(AUTH_TOKEN)).thenReturn(someCaseWithState(AWAITING_DECREE_NISI));
        when(caseMaintenanceClient.updateCase(AUTH_TOKEN, TEST_CASE_ID, CO_RESPONDENT_SUBMISSION_AWAITING_DN_EVENT_ID, submissionData))
            .thenReturn(caseUpdateResponse);

        assertThat(submitCoRespondentAosCase.execute(taskContext, submissionData), is(caseUpdateResponse));

        verify(caseMaintenanceClient)
            .updateCase(AUTH_TOKEN, TEST_CASE_ID, CO_RESPONDENT_SUBMISSION_AWAITING_DN_EVENT_ID, submissionData);
    }

    @Test
    public void givenCaseIsAwaitingLAReferral_whenCoRespondentSubmits_thenSubmitCorrectEvent() throws TaskException {
        final Map<String, Object> submissionData = new HashMap<>();

        final Map<String, Object> caseUpdateResponse = new HashMap<>();
        caseUpdateResponse.put(CCD_CASE_DATA_FIELD, emptyMap());

        when(caseMaintenanceClient.retrieveAosCase(AUTH_TOKEN)).thenReturn(someCaseWithState(AWAITING_LEGAL_ADVISOR_REFERRAL));
        when(caseMaintenanceClient.updateCase(AUTH_TOKEN, TEST_CASE_ID, CO_RESPONDENT_SUBMISSION_AWAITING_LA_EVENT_ID, submissionData))
            .thenReturn(caseUpdateResponse);

        assertThat(submitCoRespondentAosCase.execute(taskContext, submissionData), is(caseUpdateResponse));

        verify(caseMaintenanceClient)
            .updateCase(AUTH_TOKEN, TEST_CASE_ID, CO_RESPONDENT_SUBMISSION_AWAITING_LA_EVENT_ID, submissionData);
    }

    @Test
    public void givenCaseIsAosAwaiting_whenCoRespondentSubmits_thenSubmitCorrectEvent() throws TaskException {
        final Map<String, Object> submissionData = new HashMap<>();

        final Map<String, Object> caseUpdateResponse = new HashMap<>();
        caseUpdateResponse.put(CCD_CASE_DATA_FIELD, emptyMap());

        when(caseMaintenanceClient.retrieveAosCase(AUTH_TOKEN)).thenReturn(someCaseWithState(AOS_AWAITING));
        when(caseMaintenanceClient.updateCase(AUTH_TOKEN, TEST_CASE_ID, CO_RESPONDENT_SUBMISSION_AOS_AWAITING_EVENT_ID, submissionData))
            .thenReturn(caseUpdateResponse);

        assertThat(submitCoRespondentAosCase.execute(taskContext, submissionData), is(caseUpdateResponse));

        verify(caseMaintenanceClient)
            .updateCase(AUTH_TOKEN, TEST_CASE_ID, CO_RESPONDENT_SUBMISSION_AOS_AWAITING_EVENT_ID, submissionData);
    }

    @Test
    public void givenCaseIsAosStarted_whenCoRespondentSubmits_thenSubmitCorrectEvent() throws TaskException {
        final Map<String, Object> submissionData = new HashMap<>();

        final Map<String, Object> caseUpdateResponse = new HashMap<>();
        caseUpdateResponse.put(CCD_CASE_DATA_FIELD, emptyMap());

        when(caseMaintenanceClient.retrieveAosCase(AUTH_TOKEN)).thenReturn(someCaseWithState(AOS_STARTED));
        when(caseMaintenanceClient.updateCase(AUTH_TOKEN, TEST_CASE_ID, CO_RESPONDENT_SUBMISSION_AOS_STARTED_EVENT_ID, submissionData))
            .thenReturn(caseUpdateResponse);

        assertThat(submitCoRespondentAosCase.execute(taskContext, submissionData), is(caseUpdateResponse));

        verify(caseMaintenanceClient)
            .updateCase(AUTH_TOKEN, TEST_CASE_ID, CO_RESPONDENT_SUBMISSION_AOS_STARTED_EVENT_ID, submissionData);
    }

    @Test
    public void givenCaseIsAosSubmittedAwaitingAnswer_whenCoRespondentSubmits_thenSubmitCorrectEvent() throws TaskException {
        final Map<String, Object> submissionData = new HashMap<>();

        final Map<String, Object> caseUpdateResponse = new HashMap<>();
        caseUpdateResponse.put(CCD_CASE_DATA_FIELD, emptyMap());

        when(caseMaintenanceClient.retrieveAosCase(AUTH_TOKEN)).thenReturn(someCaseWithState(AOS_SUBMITTED_AWAITING_ANSWER));
        when(caseMaintenanceClient.updateCase(AUTH_TOKEN, TEST_CASE_ID, CO_RESPONDENT_SUBMISSION_AOS_SUBMIT_AWAIT_EVENT_ID, submissionData))
            .thenReturn(caseUpdateResponse);

        assertThat(submitCoRespondentAosCase.execute(taskContext, submissionData), is(caseUpdateResponse));

        verify(caseMaintenanceClient)
            .updateCase(AUTH_TOKEN, TEST_CASE_ID, CO_RESPONDENT_SUBMISSION_AOS_SUBMIT_AWAIT_EVENT_ID, submissionData);
    }

    @Test
    public void givenCaseIsAosOverdue_whenCoRespondentSubmits_thenSubmitCorrectEvent() throws TaskException {
        final Map<String, Object> submissionData = new HashMap<>();

        final Map<String, Object> caseUpdateResponse = new HashMap<>();
        caseUpdateResponse.put(CCD_CASE_DATA_FIELD, emptyMap());

        when(caseMaintenanceClient.retrieveAosCase(AUTH_TOKEN)).thenReturn(someCaseWithState(AOS_OVERDUE));
        when(caseMaintenanceClient.updateCase(AUTH_TOKEN, TEST_CASE_ID, CO_RESPONDENT_SUBMISSION_AOS_OVERDUE_EVENT_ID, submissionData))
            .thenReturn(caseUpdateResponse);

        assertThat(submitCoRespondentAosCase.execute(taskContext, submissionData), is(caseUpdateResponse));

        verify(caseMaintenanceClient)
            .updateCase(AUTH_TOKEN, TEST_CASE_ID, CO_RESPONDENT_SUBMISSION_AOS_OVERDUE_EVENT_ID, submissionData);
    }

    @Test
    public void givenCaseIsAosDefended_whenCoRespondentSubmits_thenSubmitCorrectEvent() throws TaskException {
        final Map<String, Object> submissionData = new HashMap<>();

        final Map<String, Object> caseUpdateResponse = new HashMap<>();
        caseUpdateResponse.put(CCD_CASE_DATA_FIELD, emptyMap());

        when(caseMaintenanceClient.retrieveAosCase(AUTH_TOKEN)).thenReturn(someCaseWithState(DEFENDED));
        when(caseMaintenanceClient.updateCase(AUTH_TOKEN, TEST_CASE_ID, CO_RESPONDENT_SUBMISSION_AOS_DEFENDED_EVENT_ID, submissionData))
            .thenReturn(caseUpdateResponse);

        assertThat(submitCoRespondentAosCase.execute(taskContext, submissionData), is(caseUpdateResponse));

        verify(caseMaintenanceClient)
            .updateCase(AUTH_TOKEN, TEST_CASE_ID, CO_RESPONDENT_SUBMISSION_AOS_DEFENDED_EVENT_ID, submissionData);
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

    private CaseDetails someCaseWithState(final String state) {
        return CaseDetails.builder().caseId(TEST_CASE_ID).state(state).build();
    }
}
