package uk.gov.hmcts.reform.divorce.orchestration.tasks.aospack.offline;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_CO_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

public class CoRespondentAosAnswersProcessorTaskTest {

    private final CoRespondentAosAnswersProcessorTask coRespondentAosAnswersProcessor = new CoRespondentAosAnswersProcessorTask();
    private TaskContext context;

    @Before
    public void setUp() {
        context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
    }

    @Test
    public void shouldAddNewField_ReceivedAosFromCoResp_Set_to_Yes() throws TaskException {
        Map<String, Object> payload = new HashMap<>();

        Map<String, Object> returnedPayload = coRespondentAosAnswersProcessor.execute(context, payload);

        assertNotEquals("Should not be empty", payload.size(), 0);
        assertEquals("Should have set 'RECEIVED_AOS_FROM_CO_RESP' field to Yes", YES_VALUE, returnedPayload.get(RECEIVED_AOS_FROM_CO_RESP));
    }

    @Test
    public void shouldAddNewEntryToCaseData() {
        Map<String, Object> caseData = new HashMap<>();

        coRespondentAosAnswersProcessor.updateReceivedAosFromCoRespondent(caseData, "property", "value");

        assertThat(caseData, hasKey("property"));
    }
}