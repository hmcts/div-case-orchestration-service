package uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextraction;

import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_GRANTED;

public class DecreeAbsoluteDataExtractorTest {

    private final DecreeAbsoluteDataExtractor classUnderTest = new DecreeAbsoluteDataExtractor("dest-email@divorce.gov.uk");

    @Test
    public void testBasicCsvExtractorValues() {
        assertThat(classUnderTest.getHeaderLine(), is("CaseReferenceNumber,DAApplicationDate,DNPronouncementDate,PartyApplyingForDA"));
        assertThat(classUnderTest.getDestinationEmailAddress(), is("dest-email@divorce.gov.uk"));
        assertThat(classUnderTest.getFileNamePrefix(), is("DA"));
        List<String> relevantState = classUnderTest.getRelevantCaseStates().collect(Collectors.toList());
        assertThat(relevantState, hasSize(1));
        assertThat(relevantState, hasItem(equalTo((DIVORCE_GRANTED))));
    }

    @Test
    public void shouldTransformCaseDetails() {
        Map<String, Object> firstCaseData = new HashMap<>();
        firstCaseData.put("D8caseReference", "TEST1");
        firstCaseData.put("DecreeAbsoluteApplicationDate", "2018-06-12T16:49:00.015");
        firstCaseData.put("DecreeNisiGrantedDate", "2017-08-17");
        Map<String, Object> secondCaseData = new HashMap<>();
        secondCaseData.put("D8caseReference", "TEST2");
        secondCaseData.put("DecreeAbsoluteApplicationDate", "2018-06-24T16:49:00.015");
        secondCaseData.put("DecreeNisiGrantedDate", "2017-08-26");
        CaseDetails firstCaseDetails = CaseDetails.builder().caseData(firstCaseData).build();
        CaseDetails secondCaseDetails = CaseDetails.builder().caseData(secondCaseData).build();

        Optional<String> firstTransformedCaseData = classUnderTest.mapCaseData(firstCaseDetails);
        Optional<String> secondTransformedCaseData = classUnderTest.mapCaseData(secondCaseDetails);

        assertThat(firstTransformedCaseData.get(), is("TEST1,12/06/2018,17/08/2017,petitioner"));
        assertThat(secondTransformedCaseData.get(), is("TEST2,24/06/2018,26/08/2017,petitioner"));
    }

    @Test
    public void shouldUseDecreeAbsoluteGrantedDate_WhenDecreeAbsoluteApplicationDate_IsNotProvided() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("D8caseReference", "TEST1");
        caseData.put("DecreeAbsoluteGrantedDate", "2017-08-17T16:49:00.015");
        caseData.put("DecreeNisiGrantedDate", "2017-08-26");
        CaseDetails caseDetails = CaseDetails.builder().caseData(caseData).build();

        Optional<String> transformedCaseData = classUnderTest.mapCaseData(caseDetails);

        assertThat(transformedCaseData.get(), is("TEST1,17/08/2017,26/08/2017,petitioner"));
    }

    @Test
    public void shouldNotInsertDate_WhenDecreeNisiGrantedDate_IsNotProvided() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("D8caseReference", "TEST1");
        caseData.put("DecreeAbsoluteGrantedDate", "2017-08-17T16:49:00.015");
        CaseDetails caseDetails = CaseDetails.builder().caseData(caseData).build();

        Optional<String> transformedCaseData = classUnderTest.mapCaseData(caseDetails);

        assertThat(transformedCaseData.get(), is("TEST1,17/08/2017,,petitioner"));
    }

    @Test
    public void shouldNotAddCaseToFileWhenMandatoryFieldsAreNotPresent() {
        Map<String, Object> firstCaseData = new HashMap<>();
        firstCaseData.put("DecreeAbsoluteApplicationDate", "2018-06-12T16:49:00.015");
        firstCaseData.put("DecreeNisiGrantedDate", "2017-08-17");
        Map<String, Object> secondCaseData = new HashMap<>();
        secondCaseData.put("D8caseReference", "TEST2");
        secondCaseData.put("DecreeAbsoluteApplicationDate", "2018-06-24T16:49:00.015");
        secondCaseData.put("DecreeNisiGrantedDate", "2017-08-26");
        CaseDetails firstCaseDetails = CaseDetails.builder().caseData(firstCaseData).build();
        CaseDetails secondCaseDetails = CaseDetails.builder().caseData(secondCaseData).build();

        Optional<String> firstTransformCaseData = classUnderTest.mapCaseData(firstCaseDetails);
        Optional<String> secondTransformCaseData = classUnderTest.mapCaseData(secondCaseDetails);

        assertThat(firstTransformCaseData, is(Optional.empty()));
        assertThat(secondTransformCaseData.get(), is("TEST2,24/06/2018,26/08/2017,petitioner"));
    }

}