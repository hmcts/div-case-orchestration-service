package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import com.google.common.collect.ImmutableMap;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.InvalidDataForTaskException;
import uk.gov.hmcts.reform.divorce.utils.DateUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATETIME_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATE_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.TIME_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CoECoverLetterDataExtractor.CaseDataKeys.COSTS_CLAIM_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DatesDataExtractor.CaseDataKeys.CERTIFICATE_OF_SERVICE_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DatesDataExtractor.CaseDataKeys.DA_GRANTED_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DatesDataExtractor.CaseDataKeys.RECEIVED_SERVICE_ADDED_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DatesDataExtractor.CaseDataKeys.RECEIVED_SERVICE_APPLICATION_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DatesDataExtractor.CaseDataKeys.SERVICE_APPLICATION_DECISION_DATE;
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

    @Test(expected = InvalidDataForTaskException.class)
    public void getDaGrantedDateThrowsInvalidDataForTaskExceptionWhenNoFieldFound() {
        DatesDataExtractor.getDaGrantedDate(emptyMap());
    }

    @Test
    public void getReceivedServiceAddedDateReturnsValidValueWhenItExists() {
        Map<String, Object> caseData = buildCaseDataWithField(RECEIVED_SERVICE_ADDED_DATE, VALID_DATE_FROM_CCD);
        assertThat(DatesDataExtractor.getReceivedServiceAddedDate(caseData), is(EXPECTED_DATE));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getReceivedServiceAddedDateThrowsInvalidDataForTaskExceptionWhenNoFieldFound() {
        DatesDataExtractor.getReceivedServiceAddedDate(emptyMap());
    }

    @Test
    public void getReceivedServiceApplicationDateReturnsValidValueWhenItExists() {
        Map<String, Object> caseData = buildCaseDataWithField(RECEIVED_SERVICE_APPLICATION_DATE, VALID_DATE_FROM_CCD);
        assertThat(DatesDataExtractor.getReceivedServiceApplicationDate(caseData), is(EXPECTED_DATE));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getReceivedServiceApplicationDateThrowsInvalidDataForTaskExceptionWhenNoFieldFound() {
        DatesDataExtractor.getReceivedServiceApplicationDate(emptyMap());
    }

    @Test
    public void getReceivedServiceApplicationDateUnformattedDateReturnsValidValueWhenItExists() {
        Map<String, Object> caseData = buildCaseDataWithField(RECEIVED_SERVICE_APPLICATION_DATE, VALID_DATE_FROM_CCD);
        assertThat(DatesDataExtractor.getReceivedServiceApplicationDateUnformatted(caseData), is(VALID_DATE_FROM_CCD));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getReceivedServiceApplicationDateUnformattedThrowsInvalidDataForTaskExceptionWhenNoFieldFound() {
        DatesDataExtractor.getReceivedServiceApplicationDateUnformatted(emptyMap());
    }

    @Test
    public void getReceivedServiceAddedDateUnformattedDateReturnsValidValueWhenItExists() {
        Map<String, Object> caseData = buildCaseDataWithField(RECEIVED_SERVICE_ADDED_DATE, VALID_DATE_FROM_CCD);
        assertThat(DatesDataExtractor.getReceivedServiceAddedDateUnformatted(caseData), is(VALID_DATE_FROM_CCD));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getReceivedServiceAddedDateUnformattedThrowsInvalidDataForTaskExceptionWhenNoFieldFound() {
        DatesDataExtractor.getReceivedServiceAddedDateUnformatted(emptyMap());
    }

    @Test
    public void getCertificateOfServiceDateUnformattedDateReturnsValidValueWhenItExists() {
        Map<String, Object> caseData = buildCaseDataWithField(CERTIFICATE_OF_SERVICE_DATE, VALID_DATE_FROM_CCD);
        assertThat(DatesDataExtractor.getCertificateOfServiceDateUnformatted(caseData), is(VALID_DATE_FROM_CCD));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getCertificateOfServiceDateUnformattedThrowsInvalidDataForTaskExceptionWhenNoFieldFound() {
        DatesDataExtractor.getCertificateOfServiceDateUnformatted(emptyMap());
    }

    @Test
    public void getServiceApplicationDecisionDateUnformattedDateReturnsValidValueWhenItExists() {
        Map<String, Object> caseData = buildCaseDataWithField(SERVICE_APPLICATION_DECISION_DATE, VALID_DATE_FROM_CCD);
        assertThat(DatesDataExtractor.getServiceApplicationDecisionDateUnformatted(caseData), is(VALID_DATE_FROM_CCD));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getServiceApplicationDecisionDateUnformattedThrowsInvalidDataForTaskExceptionWhenNoFieldFound() {
        DatesDataExtractor.getServiceApplicationDecisionDateUnformatted(emptyMap());
    }

    @Test
    public void getServiceApplicationDecisionDateReturnsValidValueWhenItExists() {
        Map<String, Object> caseData = buildCaseDataWithField(SERVICE_APPLICATION_DECISION_DATE, VALID_DATE_FROM_CCD);
        assertThat(DatesDataExtractor.getServiceApplicationDecisionDate(caseData), is(EXPECTED_DATE));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getServiceApplicationDecisionDateThrowsInvalidDataForTaskExceptionWhenNoFieldFound() {
        DatesDataExtractor.getReceivedServiceApplicationDate(emptyMap());
    }

    @Test
    public void getDaGrantedDateThrowsExceptions() {
        asList("", null).forEach(daDateValue -> {
            assertThrows(
                InvalidDataForTaskException.class,
                () -> DatesDataExtractor.getDaGrantedDate(buildCaseDataWithField(DA_GRANTED_DATE, daDateValue))
            );
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
        assertThat(getDeadlineToContactCourtBy(createCaseData()), CoreMatchers.is(CONTACT_COURT_BY_DATE_FORMATTED));
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

    static Map<String, Object> createCaseData() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(COSTS_CLAIM_GRANTED, YES_VALUE);
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
