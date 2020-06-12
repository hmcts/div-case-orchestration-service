package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.InvalidDataForTaskException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATETIME_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATE_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.TIME_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CoECoverLetterDataExtractor.CaseDataKeys.COST_CLAIMED;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CoECoverLetterDataExtractor.CaseDataKeys.COURT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CoECoverLetterDataExtractor.getCourtId;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CoECoverLetterDataExtractor.getDeadlineToContactCourtBy;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CoECoverLetterDataExtractor.getHearingDate;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CoECoverLetterDataExtractor.isCostsClaimGranted;

public class CoECoverLetterDataExtractorTest {

    public static final String EXPECTED_COURT = "My court";
    public static final String HEARING_DATE = "2010-10-10";
    public static final String HEARING_DATE_FORMATTED = "10 October 2010";
    public static final String CONTACT_COURT_BY_DATE_FORMATTED = "26 September 2010";

    @Test
    public void isCostsClaimGrantedReturnsTrue() {
        Map<String, Object> caseData = createCaseData();
        assertThat(isCostsClaimGranted(caseData), is(true));
    }

    @Test
    public void isCostsClaimGrantedReturnsFalse() {
        assertThat(isCostsClaimGranted(new HashMap<>()), is(false));
        assertThat(isCostsClaimGranted(ImmutableMap.of(COST_CLAIMED, NO_VALUE)), is(false));
        assertThat(isCostsClaimGranted(ImmutableMap.of(COST_CLAIMED, "")), is(false));
        assertThat(isCostsClaimGranted(ImmutableMap.of(COST_CLAIMED, "bla")), is(false));
    }

    @Test
    public void getCourtIdReturnsValue() {
        assertThat(getCourtId(createCaseData()), is(EXPECTED_COURT));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getCourtIdThrowsInvalidDataForTaskException() {
        getCourtId(new HashMap<>());
    }

    @Test
    public void getHearingDateReturnsFormattedDate() {
        assertThat(getHearingDate(createCaseData()), is(HEARING_DATE_FORMATTED));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getHearingDateThrowsInvalidDataForTaskException() {
        getHearingDate(new HashMap<>());
    }

    @Test
    public void getDeadlineToContactCourtByReturnsFormattedDate() {
        assertThat(getDeadlineToContactCourtBy(createCaseData()), is(CONTACT_COURT_BY_DATE_FORMATTED));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getDeadlineToContactCourtByThrowsInvalidDataForTaskException() {
        getDeadlineToContactCourtBy(new HashMap<>());
    }

    private static Map<String, Object> createCaseData() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(COST_CLAIMED, YES_VALUE);
        caseData.put(COURT_ID, EXPECTED_COURT);
        caseData.put(DATETIME_OF_HEARING_CCD_FIELD, createHearingDatesList());

        return caseData;
    }

    public static List<CollectionMember<Map<String, Object>>> createHearingDatesList() {
        CollectionMember<Map<String, Object>> dateOfHearing = new CollectionMember<>();
        dateOfHearing.setValue(
            ImmutableMap.of(
                DATE_OF_HEARING_CCD_FIELD, HEARING_DATE,
                TIME_OF_HEARING_CCD_FIELD, "09:10")
        );

        return asList(dateOfHearing);
    }
}