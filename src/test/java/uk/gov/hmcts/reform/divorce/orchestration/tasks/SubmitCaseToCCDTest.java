package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PIN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PIN;

@RunWith(MockitoJUnitRunner.class)
public class SubmitCaseToCCDTest {
    private SubmitCaseToCCD submitCaseToCCD;

    @Mock
    private CaseMaintenanceClient caseMaintenanceClient;
    private Map<String, Object> payload;
    private CaseDetails caseDetails;
    private TaskContext context;

    @Before
    public void setUp() throws Exception {
        submitCaseToCCD = new SubmitCaseToCCD(caseMaintenanceClient);

        payload = new HashMap<>();
        payload.put("D8ScreenHasMarriageBroken", "YES");
        payload.put(PIN,TEST_PIN );

        caseDetails = CaseDetails.builder()
                .caseId(TEST_CASE_ID)
                .state(TEST_STATE)
                .caseData(payload)
                .build();

        context = new DefaultTaskContext();

    }

    @Test
    public void executeShouldReturnUpdatedPayloadForValidCase() throws TaskException {
        //given
        when(caseMaintenanceClient.submitCase(any(), anyString())).thenReturn(payload);

        //when
        Map<String, Object> response = submitCaseToCCD.execute(context, payload, AUTH_TOKEN, caseDetails);

        //then
        assertNotNull(response);
        assertEquals(2, response.size());
        assertTrue(response.containsKey(PIN));
    }

    @After
    public void tearDown() throws Exception {
        submitCaseToCCD = null;
    }

}