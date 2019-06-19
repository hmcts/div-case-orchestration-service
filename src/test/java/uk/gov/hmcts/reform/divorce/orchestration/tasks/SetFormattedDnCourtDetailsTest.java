package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.DnCourt;
import uk.gov.hmcts.reform.divorce.orchestration.exception.CourtDetailsNotFound;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.COURT_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COURT_CONTACT_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_COURT_DETAILS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LINE_SEPARATOR;

@RunWith(MockitoJUnitRunner.class)
public class SetFormattedDnCourtDetailsTest {

    private static final String TEST_COURT_ID = "liverpool";
    private static final String TEST_COURT_NAME    = "testCourt";
    private static final String TEST_COURT_ADDRESS = "testAddress\nLineTwo\nLineThree";
    private static final String TEST_COURT_EMAIL   = "testEmail@test.test";
    private static final String TEST_COURT_PHONE   = "01234567890";

    @Mock
    private TaskCommons taskCommons;

    @InjectMocks
    private SetFormattedDnCourtDetails setFormattedDnCourtDetails;

    @Test
    public void testDnCourtDetailsAreSetOnContext() throws CourtDetailsNotFound {
        Map<String, Object> payload = new HashMap<>();
        payload.put(COURT_NAME_CCD_FIELD, TEST_COURT_ID);

        DnCourt dnCourt = new DnCourt();
        dnCourt.setName(TEST_COURT_NAME);
        dnCourt.setAddress(TEST_COURT_ADDRESS);
        dnCourt.setEmail(TEST_COURT_EMAIL);
        dnCourt.setPhone(TEST_COURT_PHONE);

        when(taskCommons.getDnCourt(TEST_COURT_ID)).thenReturn(dnCourt);

        TaskContext context = new DefaultTaskContext();

        Map<String, Object> outputPayload = setFormattedDnCourtDetails.execute(context, payload);

        Map<String, Object> contextDnCourtDetails = context.getTransientObject(DN_COURT_DETAILS);

        assertEquals(contextDnCourtDetails.get(COURT_NAME_CCD_FIELD), TEST_COURT_NAME);
        assertEquals(contextDnCourtDetails.get(COURT_CONTACT_JSON_KEY),
                TEST_COURT_ADDRESS + LINE_SEPARATOR + TEST_COURT_EMAIL + LINE_SEPARATOR + TEST_COURT_PHONE);
        assertEquals(outputPayload, payload);
    }

    @Test
    public void testDnCourtDetailsAreNotSetOnContextWhenCourtNotFound() throws CourtDetailsNotFound {
        Map<String, Object> payload = new HashMap<>();
        payload.put(COURT_NAME_CCD_FIELD, TEST_COURT_ID);

        when(taskCommons.getDnCourt(TEST_COURT_ID)).thenThrow(CourtDetailsNotFound.class);

        TaskContext context = new DefaultTaskContext();

        Map<String, Object> outputPayload = setFormattedDnCourtDetails.execute(context, payload);

        assertNull(context.getTransientObject(DN_COURT_DETAILS));
        assertEquals(outputPayload, payload);
    }

    @Test
    public void testDnCourtDetailsAreNotSetOnContextWhenNoCourtName() {
        Map<String, Object> payload = new HashMap<>();

        Map<String, Object> outputPayload = setFormattedDnCourtDetails.execute(null, payload);

        assertEquals(outputPayload, payload);
    }
}