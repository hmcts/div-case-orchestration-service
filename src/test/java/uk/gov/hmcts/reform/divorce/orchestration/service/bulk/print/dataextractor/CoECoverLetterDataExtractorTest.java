package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.Gender;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.Relation;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.InvalidDataForTaskException;

import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.EMPTY_STRING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CoECoverLetterDataExtractor.CaseDataKeys.COSTS_CLAIM_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CoECoverLetterDataExtractor.CaseDataKeys.COURT_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CoECoverLetterDataExtractor.CaseDataKeys.JUDGE_COSTS_CLAIM_GRANTED;
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
    public void isLegalAdvisorCostsClaimGrantedReturnsTrueWhenValueIsYes() {
        Map<String, Object> caseData = buildCaseDataWithIsLegalAdvisorCostsClaimGranted(YES_VALUE);
        assertThat(CoECoverLetterDataExtractor.isLegalAdvisorCostsClaimGranted(caseData), is(true));
    }

    @Test
    public void isLegalAdvisorCostsClaimGrantedReturnsFalseWhenItValueIsNoOrItDoesNotExist() {
        asList(EMPTY_STRING, null, NO_VALUE).forEach(isLegalAdvisorCostsClaimGrantedValue -> {
            Map<String, Object> caseData = buildCaseDataWithIsLegalAdvisorCostsClaimGranted(isLegalAdvisorCostsClaimGrantedValue);
            assertThat(CoECoverLetterDataExtractor.isLegalAdvisorCostsClaimGranted(caseData), is(false));
        });
    }

    @Test
    public void isCostsClaimGrantedReturnsTrueWhenJudgeCostsClaimGrantedIsYes() {
        Map<String, Object> caseData = buildCaseDataWithIsCostsClaimGranted(YES_VALUE, null);
        assertThat(CoECoverLetterDataExtractor.isCostsClaimGranted(caseData), is(true));
    }

    @Test
    public void isCostsClaimGrantedReturnsTrueWhenJudgeCostsClaimGrantedIsEmptyAndLegalAdvisorCostsClaimGrantedIsYes() {
        asList(EMPTY_STRING, null).forEach(isJudgeCostsClaimGrantedValue -> {
            Map<String, Object> caseData = buildCaseDataWithIsCostsClaimGranted(isJudgeCostsClaimGrantedValue, YES_VALUE);
            assertThat(CoECoverLetterDataExtractor.isCostsClaimGranted(caseData), is(true));
        });
    }

    @Test
    public void isCostsClaimGrantedReturnsFalseWhenJudgeCostsClaimGrantedIsNotYes() {
        asList(NO_VALUE, "adjourn").forEach(isJudgeCostsClaimGrantedValue -> {
            Map<String, Object> caseData = buildCaseDataWithIsCostsClaimGranted(isJudgeCostsClaimGrantedValue, YES_VALUE);
            assertThat(CoECoverLetterDataExtractor.isCostsClaimGranted(caseData), is(false));
        });
    }

    @Test
    public void isCostsClaimGrantedReturnsFalseWhenJudgeCostsClaimGrantedIsEmptyAndLegalAdvisorCostsClaimGrantedIsEmptyOrNo() {
        asList(EMPTY_STRING, null, NO_VALUE).forEach(isLegalAdvisorCostsClaimGrantedValue -> {
            Map<String, Object> caseData = buildCaseDataWithIsCostsClaimGranted(EMPTY_STRING, isLegalAdvisorCostsClaimGrantedValue);
            assertThat(CoECoverLetterDataExtractor.isCostsClaimGranted(caseData), is(false));
        });
    }

    @Test
    public void getHusbandOrWifeThrowsExceptionsPetitionerGenderDoesNotExist() {
        asList(EMPTY_STRING, null).forEach(petitionerGenderValue -> {
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
        asList(EMPTY_STRING, null).forEach(courtIdValue -> {
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

    private static Map<String, Object> buildCaseDataWithIsCostsClaimGranted(String isJudgeCostsClaimGranted, String isLegalAdvisorCostsClaimGranted) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(JUDGE_COSTS_CLAIM_GRANTED, isJudgeCostsClaimGranted);
        caseData.put(COSTS_CLAIM_GRANTED, isLegalAdvisorCostsClaimGranted);

        return caseData;
    }

    private static Map<String, Object> buildCaseDataWithIsLegalAdvisorCostsClaimGranted(String isLegalAdvisorCostsClaimGranted) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(COSTS_CLAIM_GRANTED, isLegalAdvisorCostsClaimGranted);

        return caseData;
    }

    /*
     * workaround for indicating that eg exception catch is what we exactly need to pass test
     */
    private static void thisTestPassed() {
        assertThat(true, is(true));
    }
}
