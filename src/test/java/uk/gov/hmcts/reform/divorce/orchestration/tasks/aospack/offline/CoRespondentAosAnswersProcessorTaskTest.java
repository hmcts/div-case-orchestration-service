package uk.gov.hmcts.reform.divorce.orchestration.tasks.aospack.offline;

import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_CO_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

public class CoRespondentAosAnswersProcessorTaskTest {

    private final CoRespondentAosAnswersProcessorTask coRespondentAosAnswersProcessor = new CoRespondentAosAnswersProcessorTask();

    @Test
    public void shouldAddNewField_ReceivedAosFromCoResp_Set_to_Yes() throws TaskException {
        Map<String, Object> payload = new HashMap<>();

        Map<String, Object> returnedPayload = coRespondentAosAnswersProcessor.execute(null, payload);

        assertNotEquals("Should not be empty", payload.size(), 0);
        assertEquals("Should have set 'RECEIVED_AOS_FROM_CO_RESP' field to Yes", YES_VALUE, returnedPayload.get(RECEIVED_AOS_FROM_CO_RESP));
    }

    @Test
    public void shouldAddNewEntryToCaseData() {
        Map<String, Object> caseData = new HashMap<>();

        coRespondentAosAnswersProcessor.addNewItemToCaseData(caseData, "property", "value");

        assertThat(caseData, hasKey("property"));
    }
}