package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_SESSION_EXISTING_PAYMENTS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PAYMENTS;

@RunWith(MockitoJUnitRunner.class)
public class PopulateExistingCollectionsUTest {

    @Mock
    private CaseMaintenanceClient caseMaintenanceClient;

    @InjectMocks
    private PopulateExistingCollections target;

    @Test
    public void givenCaseIdWithCaseData_whenExecute_thenReturnUpdatedSession() {
        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);

        CollectionMember<Map<String, Object>> existingPayments = new CollectionMember<>();
        existingPayments.setValue(Collections.EMPTY_MAP);

        List<CollectionMember> payments = Collections.singletonList(existingPayments);

        Map<String, Object> caseData = Collections.singletonMap(D_8_PAYMENTS, payments);

        when(caseMaintenanceClient.retrievePetitionById(AUTH_TOKEN, TEST_CASE_ID))
                .thenReturn(CaseDetails.builder().caseData(caseData).build());

        Map<String, Object> session  = new HashMap<>();
        Map<String, Object> expectedSession = Collections.singletonMap(DIVORCE_SESSION_EXISTING_PAYMENTS, payments);

        assertEquals(expectedSession, target.execute(context, session));
    }

    @Test
    public void givenCaseIdWithoutData_whenExecute_thenReturnSession() {
        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);

        when(caseMaintenanceClient.retrievePetitionById(AUTH_TOKEN, TEST_CASE_ID))
                .thenReturn(CaseDetails.builder().caseId(TEST_CASE_ID).build());

        Map<String, Object> session  = new HashMap<>();

        assertEquals(session, target.execute(context, session));
    }
}
