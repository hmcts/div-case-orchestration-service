package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;

import java.util.Collections;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_COURT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_DIVORCE_UNIT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.IS_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LINK_RESPONDENT_GENERIC_EVENT_ID;

@RunWith(MockitoJUnitRunner.class)
public class SetSolicitorLinkedFieldTest {

    private static final String RESP_SOLICITOR_LINKED_EMAIL = "RespSolicitorLinkedEmail";
    private static final String SOL_EMAIL = "sol@test.local";

    @Mock
    private AuthUtil authUtil;

    @Mock
    private CaseMaintenanceClient caseMaintenanceClient;

    @InjectMocks
    private SetSolicitorLinkedField setSolicitorLinkedField;

    @Test
    public void whenTaskIsCalled_setSolicitorLinkedFieldToTrue() throws TaskException {

        final UserDetails payload = UserDetails.builder().email(SOL_EMAIL).build();

        final Map<String, Object> caseData = Collections.singletonMap(D_8_DIVORCE_UNIT, TEST_COURT);
        final CaseDetails caseDetails =
                CaseDetails.builder()
                        .caseId(TEST_CASE_ID)
                        .state(LINK_RESPONDENT_GENERIC_EVENT_ID)
                        .caseData(caseData)
                        .build();

        final Map<String, Object> dataToUpdate =
                ImmutableMap.of(
                        RESP_SOLICITOR_LINKED_EMAIL, SOL_EMAIL
                );

        final TaskContext taskContext = new DefaultTaskContext();
        taskContext.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        taskContext.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
        taskContext.setTransientObject(IS_RESPONDENT, true);
        taskContext.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);

        when(authUtil.getCaseworkerToken()).thenReturn(AUTH_TOKEN);

        when(caseMaintenanceClient.updateCase(AUTH_TOKEN, TEST_CASE_ID, LINK_RESPONDENT_GENERIC_EVENT_ID, dataToUpdate))
                .thenReturn(null);

        Assert.assertEquals(payload, setSolicitorLinkedField.execute(taskContext, payload));

        verify(caseMaintenanceClient).updateCase(AUTH_TOKEN, TEST_CASE_ID, LINK_RESPONDENT_GENERIC_EVENT_ID, dataToUpdate);
    }
}