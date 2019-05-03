package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.time.Clock;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertThat;

public class AddDecreeNisiGrantedDateToPayloadTaskTest {

    private static final String DECREE_NISI_GRANTED_DATE_CCD_FIELD = "DecreeNisiGrantedDate";

    private CcdUtil ccdUtil = new CcdUtil(Clock.systemDefaultZone());

    @Test
    public void shouldAddDecreeNisiGrantedDateToPayload() throws TaskException {
        AddDecreeNisiGrantedDateToPayloadTask task = new AddDecreeNisiGrantedDateToPayloadTask(ccdUtil);

        Map<String, Object> returnedPayload = task.execute(null, singletonMap("inputTestKey", "inputTestValue"));

        assertThat(returnedPayload, allOf(
            hasEntry("inputTestKey", "inputTestValue"),
            hasEntry(DECREE_NISI_GRANTED_DATE_CCD_FIELD, ccdUtil.getCurrentDateCcdFormat())
        ));
    }

}