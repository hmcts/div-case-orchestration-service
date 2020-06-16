package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import com.google.common.collect.ImmutableMap;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.InvalidDataForTaskException;
import uk.gov.hmcts.reform.divorce.utils.DateUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATETIME_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATE_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.TIME_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CoECoverLetterDataExtractor.CaseDataKeys.COST_CLAIMED;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DatesDataExtractor.CaseDataKeys.DA_GRANTED_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DatesDataExtractor.getDeadlineToContactCourtBy;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DatesDataExtractor.getHearingDate;

public class DatesDataExtractorTest {

    public static final String CONTACT_COURT_BY_DATE_FORMATTED = "26 September 2010";
    public static final String VALID_DATE_FROM_CCD = "2010-10-01";
    public static final String EXPECTED_DATE = "1 October 2010";
    public static final String HEARING_DATE = "2010-10-10";
    public static final String HEARING_DATE_FORMATTED = "10 October 2010";

    @Test
    public void getDaGrantedDateReturnsValidValueWhenItExists() {
        Map<String, Object> caseData = buildCaseDataWithField(DA_GRANTED_DATE, VALID_DATE_FROM_CCD);
        assertThat(DatesDataExtractor.getDaGrantedDate(caseData), is(EXPECTED_DATE));
    }

    @Test
    public void getDaGrantedDateThrowsExceptions() {
        asList("", null).forEach(daDateValue -> {
            try {
                DatesDataExtractor.getDaGrantedDate(buildCaseDataWithField(DA_GRANTED_DATE, daDateValue));
                fail("Should have thrown exception");
            } catch (InvalidDataForTaskException e) {
                thisTestPassed();
            }
        });
    }

    @Test
    public void getLetterDateReturnsValidValue() {
        assertThat(DatesDataExtractor.getLetterDate(), is(LocalDate.now().format(DateUtils.Formatters.CLIENT_FACING)));
    }

    @Test
    public void getHearingDateReturnsFormattedDate() {
        MatcherAssert.assertThat(getHearingDate(createCaseData()), CoreMatchers.is(HEARING_DATE_FORMATTED));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getHearingDateThrowsInvalidDataForTaskException() {
        getHearingDate(new HashMap<>());
    }

    @Test
    public void getDeadlineToContactCourtByReturnsFormattedDate() {
        MatcherAssert.assertThat(getDeadlineToContactCourtBy(createCaseData()), CoreMatchers.is(CONTACT_COURT_BY_DATE_FORMATTED));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getDeadlineToContactCourtByThrowsInvalidDataForTaskException() {
        getDeadlineToContactCourtBy(new HashMap<>());
    }

    private static Map<String, Object> buildCaseDataWithField(String field, String value) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(field, value);

        return caseData;
    }

    /*
     * workaround for indicating that eg exception catch is what we exactly need to pass test
     */
    private static void thisTestPassed() {
        assertThat(true, is(true));
    }


    static Map<String, Object> createCaseData() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(COST_CLAIMED, YES_VALUE);
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
