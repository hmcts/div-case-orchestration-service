package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CERTIFICATE_OF_SERVICE_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.BailiffServiceApplicationDataExtractor.getBailiffApplicationGranted;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.BailiffServiceApplicationDataExtractor.getBailiffServiceSuccessful;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.BailiffServiceApplicationDataExtractor.getCertificateOfServiceDate;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.BailiffServiceApplicationDataExtractor.getLocalCourtAddress;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.BailiffServiceApplicationDataExtractor.getLocalCourtEmail;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.BailiffServiceApplicationDataExtractor.getReasonFailureToServe;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.ServiceApplicationDataExtractorTest.buildCaseDataWithField;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.BailiffServiceApplicationDataTaskTest.TEST_COURT_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.BailiffServiceApplicationDataTaskTest.TEST_COURT_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.BailiffServiceApplicationDataTaskTest.TEST_REASON_FAILURE_TO_SERVE;

public class BailiffServiceApplicationDataExtractorTest {

    @Test
    public void getLocalCourtAddressShouldReturnValidValue() {
        Map<String, Object> caseData = buildCaseDataWithField(
            CcdFields.LOCAL_COURT_ADDRESS,
            TEST_COURT_ADDRESS
        );
        assertThat(getLocalCourtAddress(caseData), is(TEST_COURT_ADDRESS));
    }

    @Test
    public void getLocalCourtEmailShouldReturnValidValue() {
        Map<String, Object> caseData = buildCaseDataWithField(
            CcdFields.LOCAL_COURT_EMAIL,
            TEST_COURT_EMAIL
        );
        assertThat(getLocalCourtEmail(caseData), is(TEST_COURT_EMAIL));
    }

    @Test
    public void getCertificateOfServiceDateShouldReturnValidValue() {
        Map<String, Object> caseData = buildCaseDataWithField(
            CcdFields.CERTIFICATE_OF_SERVICE_DATE,
            TEST_CERTIFICATE_OF_SERVICE_DATE
        );
        assertThat(getCertificateOfServiceDate(caseData), is(TEST_CERTIFICATE_OF_SERVICE_DATE));
    }

    @Test
    public void getBailiffServiceSuccessfulShouldReturnValidValue() {
        Map<String, Object> caseData = buildCaseDataWithField(
            CcdFields.BAILIFF_SERVICE_SUCCESSFUL,
            YES_VALUE
        );
        assertThat(getBailiffServiceSuccessful(caseData), is(YES_VALUE));
    }

    @Test
    public void getBailiffApplicationGrantedShouldReturnValidValue() {
        Map<String, Object> caseData = buildCaseDataWithField(
            CcdFields.BAILIFF_APPLICATION_GRANTED,
            YES_VALUE
        );
        assertThat(getBailiffApplicationGranted(caseData), is(YES_VALUE));
    }

    @Test
    public void getReasonFailureToServeShouldReturnValidValue() {
        Map<String, Object> caseData = buildCaseDataWithField(
            CcdFields.REASON_FAILURE_TO_SERVE,
            TEST_REASON_FAILURE_TO_SERVE
        );
        assertThat(getReasonFailureToServe(caseData), is(TEST_REASON_FAILURE_TO_SERVE));
    }

    @Test
    public void shouldReturnEmptyStringWhenNoValue() {
        assertThat(getLocalCourtAddress(emptyMap()), is(""));
        assertThat(getLocalCourtEmail(emptyMap()), is(""));
        assertThat(getBailiffServiceSuccessful(emptyMap()), is(""));
        assertThat(getBailiffApplicationGranted(emptyMap()), is(""));
        assertThat(getCertificateOfServiceDate(emptyMap()), is(""));
        assertThat(getReasonFailureToServe(emptyMap()), is(""));
    }
}
