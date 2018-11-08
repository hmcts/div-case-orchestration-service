package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.client.IdamClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.util.Collections;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.*;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.*;

@RunWith(MockitoJUnitRunner.class)
public class UpdateRespondentDetailsUTest {
    private static final int DUE_DATE_OFFSET_IN_DAYS = 7;

    @Mock
    private CaseMaintenanceClient caseMaintenanceClient;
    @Mock
    private IdamClient idamClient;

    @InjectMocks
    private UpdateRespondentDetails classUnderTest;

    @Before
    public void setup() {
        ReflectionTestUtils.setField(classUnderTest, "daysToComplete", DUE_DATE_OFFSET_IN_DAYS);
    }

    @Test
    public void whenExecute_thenProceedAsExpected() {
        final UserDetails payload = UserDetails.builder().build();

        final TaskContext taskContext = new DefaultTaskContext();
        taskContext.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        taskContext.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);

        final UserDetails respondentDetails =
            UserDetails.builder()
                .id(TEST_USER_ID)
                .email(TEST_EMAIL)
                .build();

        final Map<String, Object> CASE_DATA = Collections.singletonMap(D_8_DIVORCE_UNIT, TEST_COURT);
        final CaseDetails CASE_DETAILS =
                CaseDetails.builder()
                        .caseId(TEST_CASE_ID)
                        .state(AOS_AWAITING_STATE)
                        .caseData(CASE_DATA)
                        .build();

        final Map<String, Object> dataToUpdate =
            ImmutableMap.of(
                RESPONDENT_EMAIL_ADDRESS, TEST_EMAIL,
                RECEIVED_AOS_FROM_RESP, YES_VALUE,
                RECEIVED_AOS_FROM_RESP_DATE, CcdUtil.getCurrentDate(),
                CCD_DUE_DATE, CcdUtil.getCurrentDatePlusDays(DUE_DATE_OFFSET_IN_DAYS)
            );

        when(idamClient.retrieveUserDetails(BEARER_AUTH_TOKEN)).thenReturn(respondentDetails);
        when(caseMaintenanceClient.updateCase(AUTH_TOKEN, TEST_CASE_ID, START_AOS_EVENT_ID, dataToUpdate))
            .thenReturn(null);
        when(caseMaintenanceClient.retrieveAosCase(AUTH_TOKEN, false))
                .thenReturn(CASE_DETAILS);

        Assert.assertEquals(payload, classUnderTest.execute(taskContext, payload));

        verify(idamClient).retrieveUserDetails(BEARER_AUTH_TOKEN);
        verify(caseMaintenanceClient).updateCase(AUTH_TOKEN, TEST_CASE_ID, START_AOS_EVENT_ID, dataToUpdate);
        verify(caseMaintenanceClient).retrieveAosCase(AUTH_TOKEN, false);
    }
}