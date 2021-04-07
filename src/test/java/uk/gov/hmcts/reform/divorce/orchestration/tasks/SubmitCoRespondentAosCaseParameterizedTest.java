package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.time.ZoneOffset.UTC;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdEvents.CO_RESPONDENT_SUBMISSION_AOS_AWAITING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdEvents.CO_RESPONDENT_SUBMISSION_AOS_BAILIFF;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdEvents.CO_RESPONDENT_SUBMISSION_AOS_COMPLETED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdEvents.CO_RESPONDENT_SUBMISSION_AOS_DEFENDED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdEvents.CO_RESPONDENT_SUBMISSION_AOS_OVERDUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdEvents.CO_RESPONDENT_SUBMISSION_AOS_STARTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdEvents.CO_RESPONDENT_SUBMISSION_AOS_SUBMIT_AWAIT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdEvents.CO_RESPONDENT_SUBMISSION_AWAITING_DN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdEvents.CO_RESPONDENT_SUBMISSION_AWAITING_LA;
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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_BAILIFF_REFERRAL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_BAILIFF_SERVICE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_DECREE_NISI;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_DWP_RESPONSE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_LEGAL_ADVISOR_REFERRAL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_PROCESS_SERVER_SERVICE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.DEFENDED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.ISSUED_TO_BAILIFF;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA_FIELD;

@RunWith(Parameterized.class)
public class SubmitCoRespondentAosCaseParameterizedTest {
    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private CaseMaintenanceClient caseMaintenanceClient;

    @Mock
    private Clock clock;

    @InjectMocks
    private SubmitCoRespondentAosCase submitCoRespondentAosCase;

    private final TaskContext taskContext = new DefaultTaskContext();
    private static final LocalDateTime FIXED_DATE_TIME = LocalDateTime.of(2015, 12, 25, 00, 00);

    @Before
    public void setup() {
        taskContext.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        taskContext.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);

        when(clock.instant()).thenReturn(FIXED_DATE_TIME.toInstant(ZoneOffset.UTC));
        when(clock.getZone()).thenReturn(UTC);
    }

    private String caseState;
    private String eventToSubmit;

    public SubmitCoRespondentAosCaseParameterizedTest(String caseState, String eventToSubmit) {
        this.caseState = caseState;
        this.eventToSubmit = eventToSubmit;
    }

    @Parameters
    public static Collection<Object[]> getCaseStateToEventTriggerList() {
        return asList(new Object[][] {
            {AOS_COMPLETED, CO_RESPONDENT_SUBMISSION_AOS_COMPLETED},
            {AWAITING_DECREE_NISI, CO_RESPONDENT_SUBMISSION_AWAITING_DN},
            {AWAITING_LEGAL_ADVISOR_REFERRAL, CO_RESPONDENT_SUBMISSION_AWAITING_LA},
            {AOS_AWAITING, CO_RESPONDENT_SUBMISSION_AOS_AWAITING},
            {AOS_AWAITING_SOLICITOR, CO_RESPONDENT_SUBMISSION_AOS_AWAITING},
            {AOS_STARTED, CO_RESPONDENT_SUBMISSION_AOS_STARTED},
            {AOS_SUBMITTED_AWAITING_ANSWER, CO_RESPONDENT_SUBMISSION_AOS_SUBMIT_AWAIT},
            {AOS_OVERDUE, CO_RESPONDENT_SUBMISSION_AOS_OVERDUE},
            {DEFENDED, CO_RESPONDENT_SUBMISSION_AOS_DEFENDED},
            {AWAITING_ALTERNATIVE_SERVICE, CO_RESP_SUBMISSION_AWAITING_ALTERNATIVE_SERVICE},
            {AWAITING_PROCESS_SERVER_SERVICE, CO_RESP_SUBMISSION_AWAITING_PROCESS_SERVER_SERVICE},
            {AWAITING_DWP_RESPONSE, CO_RESP_SUBMISSION_AWAITING_DWP_RESPONSE},
            {AWAITING_BAILIFF_REFERRAL, CO_RESPONDENT_SUBMISSION_AOS_BAILIFF},
            {AWAITING_BAILIFF_SERVICE, CO_RESPONDENT_SUBMISSION_AOS_BAILIFF},
            {ISSUED_TO_BAILIFF, CO_RESPONDENT_SUBMISSION_AOS_BAILIFF}
        });
    }

    @Test
    public void givenState_WhenCoRespondentSubmits_ThenSubmitCorrectEvent() throws TaskException {
        final Map<String, Object> submissionData = new HashMap<>();

        final Map<String, Object> caseUpdateResponse = new HashMap<>();
        caseUpdateResponse.put(CCD_CASE_DATA_FIELD, emptyMap());

        when(caseMaintenanceClient.retrieveAosCase(AUTH_TOKEN)).thenReturn(someCaseWithState(caseState));
        when(caseMaintenanceClient.updateCase(AUTH_TOKEN, TEST_CASE_ID, eventToSubmit, submissionData))
            .thenReturn(caseUpdateResponse);

        assertThat(submitCoRespondentAosCase.execute(taskContext, submissionData), is(caseUpdateResponse));

        verify(caseMaintenanceClient).updateCase(AUTH_TOKEN, TEST_CASE_ID, eventToSubmit, submissionData);
    }

    private CaseDetails someCaseWithState(final String state) {
        return CaseDetails.builder().caseId(TEST_CASE_ID).state(state).build();
    }
}
