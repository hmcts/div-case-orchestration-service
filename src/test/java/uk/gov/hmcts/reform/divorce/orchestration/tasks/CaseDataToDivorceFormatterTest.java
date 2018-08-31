package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseFormatterClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.D_8_SCREEN_HAS_MARRIAGE_BROKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PIN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.YES;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PIN;

@RunWith(MockitoJUnitRunner.class)
public class CaseDataToDivorceFormatterTest {
    private CaseDataToDivorceFormatter caseDataToDivorceFormatter;

    @Mock
    private CaseFormatterClient caseFormatterClient;

    Map<String, Object> payload;
    private TaskContext context;

    @Before
    public void setUp() throws Exception {
        caseDataToDivorceFormatter = new CaseDataToDivorceFormatter(caseFormatterClient);

        payload = new HashMap<>();
        payload.put(D_8_SCREEN_HAS_MARRIAGE_BROKEN, YES);
        payload.put(PIN,TEST_PIN );

        context = new DefaultTaskContext();
    }

    @Test
    public void executeShouldReturnValidPayloadOnCorrectRequest() throws TaskException {
        //given
        when(caseFormatterClient.transformToDivorceFormat(payload, AUTH_TOKEN)).thenReturn(payload);

        //when
        Map<String, Object> actual = caseDataToDivorceFormatter.execute(context, payload, AUTH_TOKEN, true);

        //then
        assertNotNull(actual);
        assertEquals(2, actual.size());
        assertTrue(actual.containsKey(PIN));
    }

    @After
    public void tearDown() throws Exception {
        caseDataToDivorceFormatter = null;
    }
}