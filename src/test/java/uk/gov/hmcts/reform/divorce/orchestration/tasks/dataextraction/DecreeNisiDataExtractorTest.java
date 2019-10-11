package uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextraction;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.DnCourt;
import uk.gov.hmcts.reform.divorce.orchestration.exception.CourtDetailsNotFound;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.TaskCommons;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.D8_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_COURT;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_JUDGE_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.COURT_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COSTS_CLAIM_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATETIME_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATE_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_DECISION_DATE_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PRONOUNCEMENT_JUDGE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.TIME_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WHO_PAYS_COSTS_CCD_FIELD;

@RunWith(MockitoJUnitRunner.class)
public class DecreeNisiDataExtractorTest {

    @Rule
    public ExpectedException expectedException = none();

    @Mock
    private TaskCommons taskCommons;

    private DecreeNisiDataExtractor classUnderTest;

    private static final String TEST_DIVORCE_COSTS_CLAIM_CCD_FIELD = "Yes";
    private static final String TEST_WHO_PAYS_COSTS_CCD_FIELD = "Respondent";
    private static final String TEST_COSTS_CLAIM_GRANTED = "Yes";
    private static final String TEST_DN_DECISION_DATE = "2020-12-15";
    private static final List<Map<String, Object>> DATE_TIME_OF_HEARINGS = asList(singletonMap("value", ImmutableMap.of(
        DATE_OF_HEARING_CCD_FIELD, "2020-12-10",
        TIME_OF_HEARING_CCD_FIELD, "15:30"
    )));

    @Before
    public void setUp() throws CourtDetailsNotFound {
        classUnderTest = new DecreeNisiDataExtractor("dest-email@divorce.gov.uk", taskCommons);

        DnCourt dnCourt = new DnCourt();
        dnCourt.setName("Courts & Tribunals Service Centre");
        when(taskCommons.getDnCourt(TEST_COURT)).thenReturn(dnCourt);
        when(taskCommons.getDnCourt("invalid-court-name")).thenThrow(CourtDetailsNotFound.class);
    }

    @Test
    public void testBasicCsvExtractorValues() {
        String header = "CaseReferenceNumber,CofEGrantedDate,HearingDate,HearingTime,PlaceOfHearing,OrderForCosts,"
            + "PartyToPayCosts,CostsToBeAssessed,OrderForAncilliaryRelief,OrderOrCauseList,JudgesName";

        assertThat(classUnderTest.getHeaderLine(), is(header));
        assertThat(classUnderTest.getDestinationEmailAddress(), is("dest-email@divorce.gov.uk"));
        assertThat(classUnderTest.getFileNamePrefix(), is("DN"));
    }

    @Test
    public void shouldTransformCaseDetails() throws TaskException {
        Map<String, Object> firstCaseData = getTestCaseData();
        Map<String, Object> secondCaseData = getTestCaseData();
        secondCaseData.put(D_8_CASE_REFERENCE, "Test2");

        CaseDetails firstCaseDetails = CaseDetails.builder().caseData(firstCaseData).build();
        CaseDetails secondCaseDetails = CaseDetails.builder().caseData(secondCaseData).build();
        Optional<String> firstTransformedCaseData = classUnderTest.mapCaseData(firstCaseDetails);
        Optional<String> secondTransformedCaseData = classUnderTest.mapCaseData(secondCaseDetails);

        assertThat(firstTransformedCaseData.get(), is("LV17D80101,15/12/2020,10/12/2020,15:30,"
                + "Courts & Tribunals Service Centre,Yes,Respondent,Yes,No,Order,Judge name"));
        assertThat(secondTransformedCaseData.get(), is("Test2,15/12/2020,10/12/2020,15:30,"
                + "Courts & Tribunals Service Centre,Yes,Respondent,Yes,No,Order,Judge name"));
    }

    @Test
    public void testWhoPaysCosts_isEmpty_whenNotPresent() throws TaskException {
        Map<String, Object> caseData = getTestCaseData();
        caseData.remove(WHO_PAYS_COSTS_CCD_FIELD, TEST_WHO_PAYS_COSTS_CCD_FIELD);

        CaseDetails caseDetails = CaseDetails.builder().caseData(caseData).build();
        Optional<String> transformedCaseData = classUnderTest.mapCaseData(caseDetails);

        assertThat(transformedCaseData.get(), is("LV17D80101,15/12/2020,10/12/2020,15:30,"
                + "Courts & Tribunals Service Centre,Yes,,Yes,No,Order,Judge name"));
    }

    @Test
    public void testMissingMandatoryFieldIsMissing_shouldNotAddCaseToFile() throws TaskException {
        Map<String, Object> caseData = getTestCaseData();
        caseData.remove(D_8_CASE_REFERENCE, D8_CASE_ID);

        CaseDetails caseDetails = CaseDetails.builder().caseData(caseData).build();
        Optional<String> transformedCaseData = classUnderTest.mapCaseData(caseDetails);

        assertThat(transformedCaseData, is(Optional.empty()));
    }

    @Test
    public void shouldRethrowExceptionWithCaseId() throws TaskException {
        expectedException.expectMessage("CSV extraction failed for case id testCaseId");
        expectedException.expectCause(instanceOf(Throwable.class));
        Map<String, Object> caseData = getTestCaseData();
        caseData.put(DATETIME_OF_HEARING_CCD_FIELD, asList("not", "expected", "data"));

        CaseDetails caseDetails = CaseDetails.builder().caseId("testCaseId").caseData(caseData).build();
        Optional<String> transformedCaseData = classUnderTest.mapCaseData(caseDetails);

        assertThat(transformedCaseData, is(Optional.empty()));
    }

    @Test
    public void testCostsClaimGranted_defaultsToNo_whenMissing() throws TaskException {
        Map<String, Object> caseData = getTestCaseData();
        caseData.remove(COSTS_CLAIM_GRANTED, TEST_COSTS_CLAIM_GRANTED);

        CaseDetails caseDetails = CaseDetails.builder().caseData(caseData).build();
        Optional<String> transformedCaseData = classUnderTest.mapCaseData(caseDetails);

        assertThat(transformedCaseData.get(), is("LV17D80101,15/12/2020,10/12/2020,15:30,"
                + "Courts & Tribunals Service Centre,Yes,Respondent,No,No,Order,Judge name"));

    }

    @Test
    public void shouldReturnCourtId_ifFullCourtNameIsNotFound() throws TaskException {
        Map<String, Object> caseData = getTestCaseData();
        caseData.put(COURT_NAME_CCD_FIELD, "invalid-court-name");

        CaseDetails caseDetails = CaseDetails.builder().caseData(caseData).build();
        Optional<String> transformedCaseData = classUnderTest.mapCaseData(caseDetails);

        assertThat(transformedCaseData.get(), is("LV17D80101,15/12/2020,10/12/2020,15:30,invalid-court-name,"
                + "Yes,Respondent,Yes,No,Order,Judge name"));
    }

    private Map<String, Object> getTestCaseData() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(D_8_CASE_REFERENCE, D8_CASE_ID);
        caseData.put(DN_DECISION_DATE_FIELD, TEST_DN_DECISION_DATE);

        caseData.put(DATETIME_OF_HEARING_CCD_FIELD, DATE_TIME_OF_HEARINGS);
        caseData.put(COURT_NAME_CCD_FIELD, TEST_COURT);
        caseData.put(DIVORCE_COSTS_CLAIM_CCD_FIELD, TEST_DIVORCE_COSTS_CLAIM_CCD_FIELD);
        caseData.put(WHO_PAYS_COSTS_CCD_FIELD, TEST_WHO_PAYS_COSTS_CCD_FIELD);
        caseData.put(COSTS_CLAIM_GRANTED, TEST_COSTS_CLAIM_GRANTED);
        caseData.put(PRONOUNCEMENT_JUDGE_CCD_FIELD, TEST_JUDGE_NAME);

        return caseData;
    }
}