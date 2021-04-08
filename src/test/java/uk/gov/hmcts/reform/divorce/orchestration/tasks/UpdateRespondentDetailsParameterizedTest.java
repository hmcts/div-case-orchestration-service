package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
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
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.BEARER_AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_COURT;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdEvents.START_AOS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_AWAITING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_ALTERNATIVE_SERVICE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_BAILIFF_REFERRAL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_BAILIFF_SERVICE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_DWP_RESPONSE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_PROCESS_SERVER_SERVICE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.ISSUED_TO_BAILIFF;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_DIVORCE_UNIT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.IS_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_EMAIL_ADDRESS;

@RunWith(Parameterized.class)
public class UpdateRespondentDetailsParameterizedTest {
    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private CaseMaintenanceClient caseMaintenanceClient;

    @Mock
    private AuthUtil authUtil;

    @Mock
    private IdamClient idamClient;

    @InjectMocks
    private UpdateRespondentDetails classUnderTest;

    @Before
    public void setup() {
        when(authUtil.getBearerToken(AUTH_TOKEN)).thenCallRealMethod();
    }

    private String caseState;

    public UpdateRespondentDetailsParameterizedTest(String caseState) {
        this.caseState = caseState;
    }

    @Parameters
    public static List<String> getAosStartedEligibleStatesWhenCaseIsLinked() {
        return ImmutableList.of(
            AOS_AWAITING,
            AWAITING_ALTERNATIVE_SERVICE,
            AWAITING_PROCESS_SERVER_SERVICE,
            AWAITING_DWP_RESPONSE,
            AWAITING_BAILIFF_REFERRAL,
            AWAITING_BAILIFF_SERVICE,
            ISSUED_TO_BAILIFF);
    }

    @Test
    public void whenGivenState_thenTriggerStartAosEvent() throws TaskException {
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
                .state(caseState)
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

        when(idamClient.getUserDetails(BEARER_AUTH_TOKEN)).thenReturn(respondentDetails);
        when(caseMaintenanceClient.updateCase(AUTH_TOKEN, TEST_CASE_ID, START_AOS, dataToUpdate))
            .thenReturn(null);

        assertEquals(payload, classUnderTest.execute(taskContext, payload));

        verify(idamClient).getUserDetails(BEARER_AUTH_TOKEN);
        verify(caseMaintenanceClient).updateCase(AUTH_TOKEN, TEST_CASE_ID, START_AOS, dataToUpdate);
    }
}