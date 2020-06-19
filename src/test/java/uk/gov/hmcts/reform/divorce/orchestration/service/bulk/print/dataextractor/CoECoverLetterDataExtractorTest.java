package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.Gender;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.Relation;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.InvalidDataForTaskException;

import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CoECoverLetterDataExtractor.CaseDataKeys.COST_CLAIMED;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CoECoverLetterDataExtractor.CaseDataKeys.COURT_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CoECoverLetterDataExtractor.CaseDataKeys.PETITIONER_GENDER;

public class CoECoverLetterDataExtractorTest {

    private static final String VALID_COURT_ID = "birmingham";

    @Test
    public void getHusbandOrWifeReturnsHusbandWhenPetitionerIsMale() {
        Map<String, Object> caseData = buildCaseDataWithPetitionerGender(Gender.MALE.getValue());
        assertThat(CoECoverLetterDataExtractor.getHusbandOrWife(caseData), is(Relation.HUSBAND.getValue()));
    }

    @Test
    public void getHusbandOrWifeReturnsWifeWhenPetitionerIsFemale() {
        Map<String, Object> caseData = buildCaseDataWithPetitionerGender(Gender.FEMALE.getValue());
        assertThat(CoECoverLetterDataExtractor.getHusbandOrWife(caseData), is(Relation.WIFE.getValue()));
    }

    @Test
    public void getCourtIdReturnsValidValueWhenItExists() {
        Map<String, Object> caseData = buildCaseDataWithCourt(VALID_COURT_ID);
        assertThat(CoECoverLetterDataExtractor.getCourtId(caseData), is(VALID_COURT_ID));
    }

    @Test
    public void isCostsClaimGrantedReturnsTrueWhenValueIsYes() {
        Map<String, Object> caseData = buildCaseDataWithIsCostsClaimGranted(YES_VALUE);
        assertThat(CoECoverLetterDataExtractor.isCostsClaimGranted(caseData), is(true));
    }

    @Test
    public void isCostsClaimGrantedReturnsFalseWhenItValueIsNoOrItDoesNotExist() {
        asList("", null, NO_VALUE).forEach(isCostsClaimGrantedValue -> {
            Map<String, Object> caseData = buildCaseDataWithIsCostsClaimGranted(isCostsClaimGrantedValue);
            assertThat(CoECoverLetterDataExtractor.isCostsClaimGranted(caseData), is(false));
        });
    }

    @Test
    public void getHusbandOrWifeThrowsExceptionsPetitionerGenderDoesNotExist() {
        asList("", null).forEach(petitionerGenderValue -> {
            try {
                CoECoverLetterDataExtractor.getHusbandOrWife(buildCaseDataWithPetitionerGender(petitionerGenderValue));
                fail("Should have thrown exception");
            } catch (InvalidDataForTaskException e) {
                thisTestPassed();
            }
        });
    }

    @Test
    public void getCourtIdThrowsExceptionsWhenItDoesNotExist() {
        asList("", null).forEach(courtIdValue -> {
            try {
                CoECoverLetterDataExtractor.getCourtId(buildCaseDataWithCourt(courtIdValue));
                fail("Should have thrown exception");
            } catch (InvalidDataForTaskException e) {
                thisTestPassed();
            }
        });
    }

    private static Map<String, Object> buildCaseDataWithPetitionerGender(String gender) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(PETITIONER_GENDER, gender);

        return caseData;
    }

    private static Map<String, Object> buildCaseDataWithCourt(String courtId) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(COURT_NAME, courtId);

        return caseData;
    }

    private static Map<String, Object> buildCaseDataWithIsCostsClaimGranted(String isCostsClaimGranted) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(COST_CLAIMED, isCostsClaimGranted);

        return caseData;
    }

    /*
     * workaround for indicating that eg exception catch is what we exactly need to pass test
     */
    private static void thisTestPassed() {
        assertThat(true, is(true));
    }
}
