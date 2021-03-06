package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.mockito.Mock;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;
import uk.gov.hmcts.reform.divorce.orchestration.util.LocalDateToWelshStringConverter;

import java.time.Clock;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.core.AllOf.allOf;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getObjectMapperInstance;

public class AddDecreeNisiDecisionDateTaskTest {
    @Mock
    private LocalDateToWelshStringConverter localDateToWelshStringConverter;

    private static final String DN_DECISION_DATE_CCD_FIELD = "DNApprovalDate";

    private final CcdUtil ccdUtil = new CcdUtil(Clock.systemDefaultZone(), getObjectMapperInstance(), localDateToWelshStringConverter);

    @Test
    public void shouldAddDecreeNisiDecisionDateToPayload() {
        AddDecreeNisiDecisionDateTask task = new AddDecreeNisiDecisionDateTask(ccdUtil);

        Map<String, Object> returnedPayload = task.execute(null, singletonMap("inputTestKey", "inputTestValue"));

        assertThat(returnedPayload, allOf(
            hasEntry("inputTestKey", "inputTestValue"),
            hasEntry(DN_DECISION_DATE_CCD_FIELD, ccdUtil.getCurrentDateCcdFormat())
        ));
    }

}