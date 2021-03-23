package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.time.LocalDate;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.COURT_HEARING_DATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_ELIGIBLE_DATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_GRANTED_DATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PRONOUNCEMENT_JUDGE_CCD_FIELD;

@RunWith(MockitoJUnitRunner.class)
public class SetDnPronouncementDetailsTaskTest {

    @Mock
    private CcdUtil ccdUtil;

    @InjectMocks
    private SetDnPronouncementDetailsTask setDnPronouncementDetailsTask;

    private TaskContext context;

    @Before
    public void setup() {
        context = new DefaultTaskContext();
    }

    @Test
    public void testSetDnPronouncementDetailsSetsTheDateFieldsProperly() throws TaskException {
        HashMap<String, Object> payload = new HashMap<>();
        payload.put(PRONOUNCEMENT_JUDGE_CCD_FIELD, "District Judge");
        payload.put(COURT_HEARING_DATE_CCD_FIELD, "2000-01-01T10:20:55.000");

        CaseDetails caseDetails = CaseDetails.builder()
            .caseData(payload)
            .caseId(TEST_CASE_ID)
            .build();

        context.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);

        when(ccdUtil.getCurrentLocalDateTime()).thenReturn(LocalDate.of(2000, 10, 10).atStartOfDay());
        LocalDate courtHearingLocalDate = LocalDate.of(2000, 1, 1);
        when(ccdUtil.parseDecreeAbsoluteEligibleDate(courtHearingLocalDate)).thenCallRealMethod();

        setDnPronouncementDetailsTask.execute(context, payload);

        String expectedGrantedDate = "2000-01-01";
        String expectedEligibleDate = "2000-02-13";

        assertEquals(expectedGrantedDate, payload.get(DECREE_NISI_GRANTED_DATE_CCD_FIELD));
        assertEquals(expectedEligibleDate, payload.get(DECREE_ABSOLUTE_ELIGIBLE_DATE_CCD_FIELD));
        verify(ccdUtil).parseDecreeAbsoluteEligibleDate(courtHearingLocalDate);
    }

    @Test(expected = TaskException.class)
    public void testSetDnPronouncementDetailsThrowsExceptionWithMissingPronouncementJudge() throws TaskException {
        HashMap<String, Object> payload = new HashMap<>();
        payload.put(COURT_HEARING_DATE_CCD_FIELD, "2000-01-01T10:20:55.000");

        CaseDetails caseDetails = CaseDetails.builder()
            .caseData(payload)
            .caseId(TEST_CASE_ID)
            .build();

        context.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);

        setDnPronouncementDetailsTask.execute(context, payload);
    }

    @Test(expected = TaskException.class)
    public void testSetDnPronouncementDetailsThrowsExceptionWithFutureHearingDate() throws TaskException {
        HashMap<String, Object> payload = new HashMap<>();
        payload.put(PRONOUNCEMENT_JUDGE_CCD_FIELD, "District Judge");
        payload.put(COURT_HEARING_DATE_CCD_FIELD, "2000-11-11T10:20:55.000");

        CaseDetails caseDetails = CaseDetails.builder()
            .caseData(payload)
            .caseId(TEST_CASE_ID)
            .build();

        context.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);

        when(ccdUtil.getCurrentLocalDateTime()).thenReturn(LocalDate.of(2000, 10, 10).atStartOfDay());


        setDnPronouncementDetailsTask.execute(context, payload);
    }
}