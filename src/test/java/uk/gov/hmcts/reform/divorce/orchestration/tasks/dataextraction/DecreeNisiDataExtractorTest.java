package uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextraction;

import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.D8_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_COURT;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_JUDGE_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.COURT_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COSTS_CLAIM_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATETIME_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_DECISION_DATE_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PRONOUNCEMENT_JUDGE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WHO_PAYS_COSTS_CCD_FIELD;

public class DecreeNisiDataExtractorTest {

    private final DecreeNisiDataExtractor classUnderTest = new DecreeNisiDataExtractor("dest-email@divorce.gov.uk");

    private static final String TEST_DATETIME_OF_HEARING_CCD_FIELD = "2020-12-10T15:30";
    private static final String TEST_DIVORCE_COSTS_CLAIM_CCD_FIELD = "Yes";
    private static final String TEST_WHO_PAYS_COSTS_CCD_FIELD = "Respondent";
    private static final String TEST_COSTS_CLAIM_GRANTED = "Yes";
    private static final String TEST_DN_DECISION_DATE = "2020-12-15";

    @Test
    public void testBasicCsvExtractorValues() {
        String header = "CaseReferenceNumber,CofEGrantedDate,HearingDate,HearingTime,PlaceOfHearing,OrderForCosts,"
                + "PartyToPayCosts,CostsToBeAssessed,OrderForAncilliaryRelief,OrderOrCauseList,JudgesName";

        assertThat(classUnderTest.getHeaderLine(), is(header));
        assertThat(classUnderTest.getDestinationEmailAddress(), is("dest-email@divorce.gov.uk"));
        assertThat(classUnderTest.getFileNamePrefix(), is("DN"));
    }

    @Test
    public void shouldTransformCaseDetails() {
        Map<String, Object> firstCaseData = getTestCaseData();
        Map<String, Object> secondCaseData = getTestCaseData();
        secondCaseData.put(D_8_CASE_REFERENCE, "Test2");

        CaseDetails firstCaseDetails = CaseDetails.builder().caseData(firstCaseData).build();
        CaseDetails secondCaseDetails = CaseDetails.builder().caseData(secondCaseData).build();
        Optional<String> firstTransformedCaseData = classUnderTest.mapCaseData(firstCaseDetails);
        Optional<String> secondTransformedCaseData = classUnderTest.mapCaseData(secondCaseDetails);

        assertThat(firstTransformedCaseData.get(), is("LV17D80101,15/12/2020,10/12/2020,15:30,serviceCentre,Yes,Respondent,Yes,No,Order,Judge name"));
        assertThat(secondTransformedCaseData.get(), is("Test2,15/12/2020,10/12/2020,15:30,serviceCentre,Yes,Respondent,Yes,No,Order,Judge name"));
    }

    @Test
    public void testWhoPaysCosts_isEmpty_whenNotPresent() {
        Map<String, Object> caseData = getTestCaseData();
        caseData.remove(WHO_PAYS_COSTS_CCD_FIELD, TEST_WHO_PAYS_COSTS_CCD_FIELD);

        CaseDetails caseDetails = CaseDetails.builder().caseData(caseData).build();
        Optional<String> transformedCaseData = classUnderTest.mapCaseData(caseDetails);

        assertThat(transformedCaseData.get(), is("LV17D80101,15/12/2020,10/12/2020,15:30,serviceCentre,Yes,,Yes,No,Order,Judge name"));
    }

    @Test
    public void testMissingMandatoryFieldIsMissing_shouldNotAddCaseToFile() {
        Map<String, Object> caseData = getTestCaseData();
        caseData.remove(D_8_CASE_REFERENCE, D8_CASE_ID);

        CaseDetails caseDetails = CaseDetails.builder().caseData(caseData).build();
        Optional<String> transformedCaseData = classUnderTest.mapCaseData(caseDetails);

        assertThat(transformedCaseData, is(Optional.empty()));
    }

    private Map<String, Object> getTestCaseData() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(D_8_CASE_REFERENCE, D8_CASE_ID);
        caseData.put(DN_DECISION_DATE_FIELD, TEST_DN_DECISION_DATE);
        caseData.put(DATETIME_OF_HEARING_CCD_FIELD, TEST_DATETIME_OF_HEARING_CCD_FIELD);
        caseData.put(COURT_NAME_CCD_FIELD, TEST_COURT);
        caseData.put(DIVORCE_COSTS_CLAIM_CCD_FIELD, TEST_DIVORCE_COSTS_CLAIM_CCD_FIELD);
        caseData.put(WHO_PAYS_COSTS_CCD_FIELD, TEST_WHO_PAYS_COSTS_CCD_FIELD);
        caseData.put(COSTS_CLAIM_GRANTED, TEST_COSTS_CLAIM_GRANTED);
        caseData.put("OrderForAncilliaryRelief", "No");
        caseData.put("OrderOrCauseList", "Order");
        caseData.put(PRONOUNCEMENT_JUDGE_CCD_FIELD, TEST_JUDGE_NAME);

        return caseData;
    }
}