package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.time.LocalDate;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.COURT_HEARING_DATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_ELIGIBLE_DATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_GRANTED_DATE_CCD_FIELD;

@RunWith(MockitoJUnitRunner.class)
public class SetDnGrantedDateTest {

    @Mock
    private CcdUtil ccdUtil;

    @InjectMocks
    private SetDnGrantedDate setDnGrantedDate;

    @Test
    public void testGenerateIssueDateSetsDateToNow() throws TaskException {
        HashMap<String, Object> payload = new HashMap<>();
        payload.put(COURT_HEARING_DATE_CCD_FIELD, "2000-01-01T10:20:55.000");

        LocalDate courtHearingLocalDate = LocalDate.of(2000, 1, 1);
        when(ccdUtil.parseDecreeAbsoluteEligibleDate(courtHearingLocalDate)).thenCallRealMethod();

        setDnGrantedDate.execute(null, payload);

        String expectedGrantedDate = "2000-01-01";
        String expectedEligibleDate = "2000-02-13";

        assertEquals(expectedGrantedDate, payload.get(DECREE_NISI_GRANTED_DATE_CCD_FIELD));
        assertEquals(expectedEligibleDate, payload.get(DECREE_ABSOLUTE_ELIGIBLE_DATE_CCD_FIELD));
        verify(ccdUtil).parseDecreeAbsoluteEligibleDate(courtHearingLocalDate);
    }
}