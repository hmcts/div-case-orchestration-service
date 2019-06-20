package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.time.Clock;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_OUTCOME_CHECKED_BY_LA_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

public class AddDecreeNisiApprovalDateTaskTest {

    private static final String DN_APPROVAL_DATE_CCD_FIELD = "DNApprovalDate";

    private CcdUtil ccdUtil = new CcdUtil(Clock.systemDefaultZone());

    @Test
    public void shouldAddDecreeNisiApprovalDateToPayload() {
        AddDecreeNisiApprovalDateTask task = new AddDecreeNisiApprovalDateTask(ccdUtil);

        Map<String, Object> returnedPayload = task.execute(null, singletonMap("inputTestKey", "inputTestValue"));

        assertThat(returnedPayload, allOf(
            hasEntry("inputTestKey", "inputTestValue"),
            hasEntry(DN_APPROVAL_DATE_CCD_FIELD, ccdUtil.getCurrentDateCcdFormat()),
            hasEntry(DN_OUTCOME_CHECKED_BY_LA_CCD_FIELD, YES_VALUE)

        ));
    }

}