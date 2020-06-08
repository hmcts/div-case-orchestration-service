package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.Gender;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.Relation;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CertificateOfEntitlementLetterDataExtractor.CaseDataKeys.COURT_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CertificateOfEntitlementLetterDataExtractor.CaseDataKeys.HEARING_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CertificateOfEntitlementLetterDataExtractor.CaseDataKeys.HEARING_DATE_TIME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CertificateOfEntitlementLetterDataExtractor.CaseDataKeys.IS_COSTS_CLAIM_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CertificateOfEntitlementLetterDataExtractor.CaseDataKeys.PETITIONER_GENDER;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CertificateOfEntitlementLetterDataExtractor.CaseDataKeys.SOLICITOR_REFERENCE;

public class CertificateOfEntitlementLetterDataExtractorTest {

    private static final String VALID_HEARING_DATE = "2020-10-20";
    private static final String VALID_HEARING_DATE_FORMATTED = "20 October 2020";
    private static final String VALID_LIMIT_DATE_FORMATTED = "6 October 2020"; // hearing date minus 14 days
    private static final String VALID_COURT_NAME = "The Family Court at Southampton";
    private static final String VALID_SOLICITOR_REF = "solRef123";

    @Test
    public void getHearingDateReturnsValidFormattedValueWhenItExists() throws TaskException {
        Map<String, Object> caseData = buildCaseDataWithHearingDate(VALID_HEARING_DATE);
        assertThat(CertificateOfEntitlementLetterDataExtractor.getHearingDate(caseData), is(VALID_HEARING_DATE_FORMATTED));
    }

    @Test
    public void getLimitDateToContactCourtReturnsValidFormattedValueWhenHearingDateExists() throws TaskException {
        Map<String, Object> caseData = buildCaseDataWithHearingDate(VALID_HEARING_DATE);
        assertThat(CertificateOfEntitlementLetterDataExtractor.getLimitDateToContactCourt(caseData), is(VALID_LIMIT_DATE_FORMATTED));
    }

    @Test
    public void getHusbandOrWifeReturnsHusbandWhenPetitionerIsMale() throws TaskException {
        Map<String, Object> caseData = buildCaseDataWithPetitionerGender(Gender.MALE.getValue());
        assertThat(CertificateOfEntitlementLetterDataExtractor.getHusbandOrWife(caseData), is(Relation.HUSBAND.getValue()));
    }

    @Test
    public void getHusbandOrWifeReturnsWifeWhenPetitionerIsFemale() throws TaskException {
        Map<String, Object> caseData = buildCaseDataWithPetitionerGender(Gender.FEMALE.getValue());
        assertThat(CertificateOfEntitlementLetterDataExtractor.getHusbandOrWife(caseData), is(Relation.WIFE.getValue()));
    }

    @Test
    public void getCourtNameReturnsValidValueWhenItExists() throws TaskException {
        Map<String, Object> caseData = buildCaseDataWithCourtName(VALID_COURT_NAME);
        assertThat(CertificateOfEntitlementLetterDataExtractor.getCourtName(caseData), is(VALID_COURT_NAME));
    }

    @Test
    public void isCostsClaimGrantedReturnsTrueWhenValueIsYes() throws TaskException {
        Map<String, Object> caseData = buildCaseDataWithIsCostsClaimGranted(YES_VALUE);
        assertThat(CertificateOfEntitlementLetterDataExtractor.isCostsClaimGranted(caseData), is(true));
    }

    @Test
    public void isCostsClaimGrantedReturnsFalseWhenItValueIsNoOrItDoesNotExist() throws TaskException {
        asList("", null, NO_VALUE).forEach(isCostsClaimGrantedValue -> {
            Map<String, Object> caseData = buildCaseDataWithIsCostsClaimGranted(isCostsClaimGrantedValue);
            assertThat(CertificateOfEntitlementLetterDataExtractor.isCostsClaimGranted(caseData), is(false));
        });
    }

    @Test
    public void getSolicitorReferenceReturnsValidValueWhenItExists() throws TaskException {
        Map<String, Object> caseData = buildCaseDataWithSolicitorReference(VALID_SOLICITOR_REF);
        assertThat(CertificateOfEntitlementLetterDataExtractor.getSolicitorReference(caseData), is(VALID_SOLICITOR_REF));
    }

    @Test
    public void getHearingDateThrowsExceptionsWhenItIsEmpty() {
        try {
            CertificateOfEntitlementLetterDataExtractor.getHearingDate(buildCaseDataWithHearingDate(""));
            fail("Should have thrown exception");
        } catch (TaskException e) {
            thisTestPassed();
        }
    }

    @Test
    public void getHearingDateThrowsExceptionsWhenItDoesNotExist() {
        try {
            CertificateOfEntitlementLetterDataExtractor.getHearingDate(new HashMap<>());
            fail("Should have thrown exception");
        } catch (TaskException e) {
            thisTestPassed();
        }
    }

    @Test
    public void getLimitDateToContactCourtThrowsExceptionsWhenHearingDateDoesNotExist() {
        try {
            CertificateOfEntitlementLetterDataExtractor.getLimitDateToContactCourt(buildCaseDataWithHearingDate(""));
            fail("Should have thrown exception");
        } catch (TaskException e) {
            thisTestPassed();
        }
    }

    @Test
    public void getHusbandOrWifeThrowsExceptionsPetitionerGenderDoesNotExist() {
        asList("", null).forEach(petitionerGenderValue -> {
            try {
                CertificateOfEntitlementLetterDataExtractor.getHusbandOrWife(buildCaseDataWithPetitionerGender(petitionerGenderValue));
                fail("Should have thrown exception");
            } catch (TaskException e) {
                thisTestPassed();
            }
        });
    }

    @Test
    public void getCourtNameThrowsExceptionsWhenItDoesNotExist() {
        asList("", null).forEach(courtNameValue -> {
            try {
                CertificateOfEntitlementLetterDataExtractor.getCourtName(buildCaseDataWithCourtName(courtNameValue));
                fail("Should have thrown exception");
            } catch (TaskException e) {
                thisTestPassed();
            }
        });
    }

    @Test
    public void getSolicitorReferenceThrowsExceptionsWhenItDoesNotExist() {
        asList("", null).forEach(solicitorRefValue -> {
            try {
                CertificateOfEntitlementLetterDataExtractor.getSolicitorReference(buildCaseDataWithSolicitorReference(solicitorRefValue));
                fail("Should have thrown exception");
            } catch (TaskException e) {
                thisTestPassed();
            }
        });
    }

    private static Map<String, Object> buildCaseDataWithHearingDate(String date) {
        List<Map<String, Object>> hearingDateAndTime = singletonList(singletonMap("value", ImmutableMap.of(
            HEARING_DATE, date
        )));
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(HEARING_DATE_TIME, hearingDateAndTime);

        return caseData;
    }

    private static Map<String, Object> buildCaseDataWithPetitionerGender(String gender) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(PETITIONER_GENDER, gender);

        return caseData;
    }

    private static Map<String, Object> buildCaseDataWithCourtName(String courtName) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(COURT_NAME, courtName);

        return caseData;
    }

    private static Map<String, Object> buildCaseDataWithIsCostsClaimGranted(String isCostsClaimGranted) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(IS_COSTS_CLAIM_GRANTED, isCostsClaimGranted);

        return caseData;
    }

    private static Map<String, Object> buildCaseDataWithSolicitorReference(String solicitorReference) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(SOLICITOR_REFERENCE, solicitorReference);

        return caseData;
    }

    /*
     * workaround for indicating that eg exception catch is what we exactly need to pass test
     */
    private static void thisTestPassed() {
        assertThat(true, is(true));
    }
}
