package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.client.IdamClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AOS_AWAITING_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AWAITING_CONSIDERATION_GENERAL_APPLICATION;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.BEARER_AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_COURT;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_OVERDUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_START_FROM_OVERDUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_START_FROM_REISSUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_REISSUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESP_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESP_LINKED_TO_CASE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESP_LINKED_TO_CASE_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_DIVORCE_UNIT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.IS_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LINK_RESPONDENT_GENERIC_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.START_AOS_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@RunWith(MockitoJUnitRunner.class)
public class UpdateRespondentDetailsUTest {

    private static final String FIXED_DATE = "2018-01-01";

    @Mock
    private CaseMaintenanceClient caseMaintenanceClient;

    @Mock
    private AuthUtil authUtil;

    @Mock
    private CcdUtil ccdUtil;

    @Mock
    private IdamClient idamClient;

    @InjectMocks
    private UpdateRespondentDetails classUnderTest;

    @Before
    public void setup() {
        when(ccdUtil.getCurrentDateCcdFormat()).thenReturn(FIXED_DATE);
        when(authUtil.getBearToken(AUTH_TOKEN)).thenCallRealMethod();
    }

    @Test
    public void whenAosAwaiting_thenProceedAsExpected() throws TaskException {
        final UserDetails payload = UserDetails.builder().build();

        final UserDetails respondentDetails =
            UserDetails.builder()
                .id(TEST_USER_ID)
                .email(TEST_EMAIL)
                .build();

        final Map<String, Object> caseData = Collections.singletonMap(D_8_DIVORCE_UNIT, TEST_COURT);
        final CaseDetails caseDetails =
            CaseDetails.builder()
                .caseId(TEST_CASE_ID)
                .state(AOS_AWAITING_STATE)
                .caseData(caseData)
                .build();

        final Map<String, Object> dataToUpdate =
            ImmutableMap.of(
                RESPONDENT_EMAIL_ADDRESS, TEST_EMAIL
            );

        final TaskContext taskContext = new DefaultTaskContext();
        taskContext.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        taskContext.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
        taskContext.setTransientObject(IS_RESPONDENT, true);
        taskContext.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);

        when(idamClient.retrieveUserDetails(BEARER_AUTH_TOKEN)).thenReturn(respondentDetails);
        when(caseMaintenanceClient.updateCase(AUTH_TOKEN, TEST_CASE_ID, START_AOS_EVENT_ID, dataToUpdate))
            .thenReturn(null);

        Assert.assertEquals(payload, classUnderTest.execute(taskContext, payload));

        verify(idamClient).retrieveUserDetails(BEARER_AUTH_TOKEN);
        verify(caseMaintenanceClient).updateCase(AUTH_TOKEN, TEST_CASE_ID, START_AOS_EVENT_ID, dataToUpdate);
    }

    @Test
    public void whenNonStandardState_thenProceedAsExpected() throws TaskException {
        final UserDetails payload = UserDetails.builder().build();

        final UserDetails respondentDetails =
            UserDetails.builder()
                .id(TEST_USER_ID)
                .email(TEST_EMAIL)
                .build();

        final Map<String, Object> caseData = Collections.singletonMap(D_8_DIVORCE_UNIT, TEST_COURT);

        final CaseDetails caseDetails =
            CaseDetails.builder()
                .caseId(TEST_CASE_ID)
                .state(AWAITING_CONSIDERATION_GENERAL_APPLICATION)
                .caseData(caseData)
                .build();

        final Map<String, Object> dataToUpdate =
            ImmutableMap.of(
                RESPONDENT_EMAIL_ADDRESS, TEST_EMAIL
            );

        final TaskContext taskContext = new DefaultTaskContext();
        taskContext.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        taskContext.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
        taskContext.setTransientObject(IS_RESPONDENT, true);
        taskContext.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);

        when(idamClient.retrieveUserDetails(BEARER_AUTH_TOKEN)).thenReturn(respondentDetails);

        Assert.assertEquals(payload, classUnderTest.execute(taskContext, payload));

        verify(idamClient).retrieveUserDetails(BEARER_AUTH_TOKEN);
        verify(caseMaintenanceClient).updateCase(AUTH_TOKEN, TEST_CASE_ID,
            LINK_RESPONDENT_GENERIC_EVENT_ID, dataToUpdate);
    }

    @Test
    public void givenCaseOnAosOverdueState_whenUpdateRespondentDetails_thenRespondentDetailsIsUpdated() throws TaskException {
        final UserDetails payload = UserDetails.builder().build();

        final UserDetails respondentDetails = createTestUserDetails();

        final CaseDetails caseDetails = createTestCaseDetails(AOS_OVERDUE);

        final Map<String, Object> dataToUpdate = createDataToUpdate();

        final TaskContext taskContext = new DefaultTaskContext();
        taskContext.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        taskContext.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
        taskContext.setTransientObject(IS_RESPONDENT, true);
        taskContext.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);

        when(idamClient.retrieveUserDetails(BEARER_AUTH_TOKEN))
            .thenReturn(respondentDetails);

        UserDetails result = classUnderTest.execute(taskContext, payload);
        Assert.assertEquals(payload, result);

        verify(caseMaintenanceClient).updateCase(AUTH_TOKEN, TEST_CASE_ID, AOS_START_FROM_OVERDUE, dataToUpdate);
    }

    @Test
    public void givenCaseOnReissueState_whenUpdateRespondentDetails_thenStartAosFromReissueEventIsTriggered() throws TaskException {
        final UserDetails payload = UserDetails.builder().build();

        final UserDetails respondentDetails = createTestUserDetails();

        final CaseDetails caseDetails = createTestCaseDetails(AWAITING_REISSUE);

        final Map<String, Object> dataToUpdate = createDataToUpdate();

        final TaskContext taskContext = new DefaultTaskContext();
        taskContext.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        taskContext.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
        taskContext.setTransientObject(IS_RESPONDENT, true);
        taskContext.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);

        when(idamClient.retrieveUserDetails(BEARER_AUTH_TOKEN)).thenReturn(respondentDetails);

        UserDetails result = classUnderTest.execute(taskContext, payload);
        Assert.assertEquals(payload, result);

        verify(caseMaintenanceClient).updateCase(AUTH_TOKEN, TEST_CASE_ID, AOS_START_FROM_REISSUE, dataToUpdate);
    }

    @Test
    public void whenCoRespondentData_whenUpdateRespondentDetails_thenUpdatedCoRespondentFields() throws TaskException {
        final UserDetails payload = UserDetails.builder().build();

        final UserDetails coRespondentDetails =
            UserDetails.builder()
                .id(TEST_USER_ID)
                .email(TEST_EMAIL)
                .build();

        final Map<String, Object> caseData = Collections.singletonMap(D_8_DIVORCE_UNIT, TEST_COURT);
        final CaseDetails caseDetails =
            CaseDetails.builder()
                .caseId(TEST_CASE_ID)
                .state(AOS_AWAITING_STATE)
                .caseData(caseData)
                .build();

        final TaskContext taskContext = new DefaultTaskContext();
        taskContext.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        taskContext.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
        taskContext.setTransientObject(IS_RESPONDENT, false);
        taskContext.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);

        when(idamClient.retrieveUserDetails(BEARER_AUTH_TOKEN)).thenReturn(coRespondentDetails);

        Assert.assertEquals(payload, classUnderTest.execute(taskContext, payload));

        final Map<String, Object> expectedDataToUpdate =
            ImmutableMap.of(
                CO_RESP_EMAIL_ADDRESS, TEST_EMAIL,
                CO_RESP_LINKED_TO_CASE, YES_VALUE,
                CO_RESP_LINKED_TO_CASE_DATE, FIXED_DATE
            );

        verify(idamClient).retrieveUserDetails(BEARER_AUTH_TOKEN);
        verify(caseMaintenanceClient).updateCase(eq(AUTH_TOKEN), eq(TEST_CASE_ID), eq(LINK_RESPONDENT_GENERIC_EVENT_ID), eq(expectedDataToUpdate));
    }


    private UserDetails createTestUserDetails() {
        return UserDetails.builder()
            .id(TEST_USER_ID)
            .email(TEST_EMAIL)
            .build();
    }

    private CaseDetails createTestCaseDetails(String state) {
        Map<String, Object> caseData = Collections.singletonMap(D_8_DIVORCE_UNIT, TEST_COURT);
        return CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .state(state)
            .caseData(caseData)
            .build();
    }

    private Map<String, Object> createDataToUpdate() {
        return ImmutableMap.of(
            RESPONDENT_EMAIL_ADDRESS, TEST_EMAIL
        );
    }
}