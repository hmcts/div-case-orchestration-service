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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_LEGAL_ADVISOR_REFERRAL;

public class AOSDataExtractorTest {

    private final AOSDataExtractor classUnderTest = new AOSDataExtractor("aosdn-email@divorce.gov.uk");

    @Test
    public void shouldHaveCorrectHeaderEmailAndFilePrefix() {
        assertThat(classUnderTest.getHeaderLine(),
            is("CaseReferenceNumber,ReceivedAOSFromResDate,ReceivedAOSFromCoResDate,ReceivedDNApplicationDate"));
        assertThat(classUnderTest.getDestinationEmailAddress(), is("aosdn-email@divorce.gov.uk"));
        assertThat(classUnderTest.getFileNamePrefix(), is("AOSDN"));
        List<String> relevantStates = classUnderTest.getRelevantCaseStates().collect(Collectors.toList());
        assertThat(relevantStates, hasSize(1));
        assertThat(relevantStates, hasItem(equalTo(AWAITING_LEGAL_ADVISOR_REFERRAL)));
    }

    @Test
    public void shouldTransformCaseDetails() {
        Map<String, Object> firstTestCaseData = new HashMap<>();
        firstTestCaseData.put("D8caseReference", "LV17D90000");
        firstTestCaseData.put("ReceivedAOSfromRespDate", "2019-07-16");
        firstTestCaseData.put("ReceivedAosFromCoRespDate", "2019-08-02");
        firstTestCaseData.put("DNApplicationSubmittedDate", "2019-09-06");

        Map<String, Object> secondTestCaseData = new HashMap<>();
        secondTestCaseData.put("D8caseReference", "LV17D90001");
        secondTestCaseData.put("ReceivedAOSfromRespDate", "2018-12-23");
        secondTestCaseData.put("ReceivedAosFromCoRespDate", "2019-01-15");
        secondTestCaseData.put("DNApplicationSubmittedDate", "2019-03-19");

        CaseDetails firstTestCaseDetails = CaseDetails.builder().caseData(firstTestCaseData).build();
        CaseDetails secondTestCaseDetails = CaseDetails.builder().caseData(secondTestCaseData).build();

        Optional<String> firstTransformedTestCaseData = classUnderTest.mapCaseData(firstTestCaseDetails);
        Optional<String> secondTransformedTestCaseData = classUnderTest.mapCaseData(secondTestCaseDetails);

        assertThat(firstTransformedTestCaseData.get(), is(System.lineSeparator() + "LV17D90000,16/07/2019,02/08/2019,06/09/2019"));
        assertThat(secondTransformedTestCaseData.get(), is(System.lineSeparator() + "LV17D90001,23/12/2018,15/01/2019,19/03/2019"));
    }

    @Test
    public void receivedAOSfromRespDate_whenNotProvided_shouldNotInsertIntoTransformedCase() {
        Map<String, Object> caseDataWithoutReceivedAOSfromRespDate = new HashMap<>();
        caseDataWithoutReceivedAOSfromRespDate.put("D8caseReference", "LV17D90002");
        caseDataWithoutReceivedAOSfromRespDate.put("ReceivedAosFromCoRespDate", "2019-02-14");
        caseDataWithoutReceivedAOSfromRespDate.put("DNApplicationSubmittedDate", "2019-04-28");

        CaseDetails caseDetails = CaseDetails.builder().caseData(caseDataWithoutReceivedAOSfromRespDate).build();
        Optional<String> transformedTestCaseData = classUnderTest.mapCaseData(caseDetails);

        assertThat(transformedTestCaseData.get(), is(System.lineSeparator() + "LV17D90002,,14/02/2019,28/04/2019"));
    }

    @Test
    public void receivedAosFromCoRespDateNotProvided_shouldNotInsertIntoTransformedCase() {
        Map<String, Object> caseDataWithoutReceivedAosFromCoRespDate = new HashMap<>();
        caseDataWithoutReceivedAosFromCoRespDate.put("D8caseReference", "LV17D90003");
        caseDataWithoutReceivedAosFromCoRespDate.put("ReceivedAOSfromRespDate", "2019-05-02");
        caseDataWithoutReceivedAosFromCoRespDate.put("DNApplicationSubmittedDate", "2019-06-10");

        CaseDetails caseDetails = CaseDetails.builder().caseData(caseDataWithoutReceivedAosFromCoRespDate).build();
        Optional<String> transformedTestCaseData = classUnderTest.mapCaseData(caseDetails);

        assertThat(transformedTestCaseData.get(), is(System.lineSeparator() + "LV17D90003,02/05/2019,,10/06/2019"));
    }

    @Test
    public void noOptionalFieldsProvided_shouldNotInsertMissingOptionalFieldsToTransformedCase() {
        Map<String, Object> caseDataWithoutReceivedAosFromCoRespDate = new HashMap<>();
        caseDataWithoutReceivedAosFromCoRespDate.put("D8caseReference", "LV17D90004");
        caseDataWithoutReceivedAosFromCoRespDate.put("DNApplicationSubmittedDate", "2019-09-12");

        CaseDetails caseDetails = CaseDetails.builder().caseData(caseDataWithoutReceivedAosFromCoRespDate).build();
        Optional<String> transformedTestCaseData = classUnderTest.mapCaseData(caseDetails);

        assertThat(transformedTestCaseData.get(), is(System.lineSeparator() + "LV17D90004,,,12/09/2019"));
    }

    @Test
    public void mandatoryCaseReferenceFieldNotPresent_shouldNotAddCaseToFile() {
        Map<String, Object> caseDataMissingCaseReference = new HashMap<>();
        caseDataMissingCaseReference.put("ReceivedAOSfromRespDate", "2019-07-16");
        caseDataMissingCaseReference.put("ReceivedAosFromCoRespDate", "2019-08-02");
        caseDataMissingCaseReference.put("DNApplicationSubmittedDate", "2019-09-06");

        CaseDetails caseDetailsMissingCaseReference = CaseDetails.builder().caseData(caseDataMissingCaseReference).build();
        Optional<String> transformedCaseMissingCaseReference = classUnderTest.mapCaseData(caseDetailsMissingCaseReference);

        assertThat(transformedCaseMissingCaseReference, is(Optional.empty()));
    }

    @Test
    public void mandatoryDNApplicationSubmittedDateNotPresent_shouldNotAddCaseToFile() {
        Map<String, Object> caseDataMissingDNApplicationSubmittedDate = new HashMap<>();
        caseDataMissingDNApplicationSubmittedDate.put("D8caseReference", "LV17D90005");
        caseDataMissingDNApplicationSubmittedDate.put("ReceivedAOSfromRespDate", "2018-12-23");
        caseDataMissingDNApplicationSubmittedDate.put("ReceivedAosFromCoRespDate", "2019-01-15");

        CaseDetails caseDetailsMissingDNApplicationSubmittedDate = CaseDetails.builder().caseData(caseDataMissingDNApplicationSubmittedDate).build();
        Optional<String> transformedCaseMissingDNApplicationSubmittedDate = classUnderTest.mapCaseData(caseDetailsMissingDNApplicationSubmittedDate);

        assertThat(transformedCaseMissingDNApplicationSubmittedDate, is(Optional.empty()));
    }

}
