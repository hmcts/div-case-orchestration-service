package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.Gender;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.Relation;

import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.EMPTY_STRING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.ExtractorHelper.getMandatoryStringValue;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getOptionalPropertyValueAsString;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CoECoverLetterDataExtractor {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CaseDataKeys {
        public static final String COURT_NAME = CcdFields.COURT_NAME;
        public static final String HEARING_DATE = OrchestrationConstants.DATE_OF_HEARING_CCD_FIELD;
        public static final String HEARING_DATE_TIME = OrchestrationConstants.DATETIME_OF_HEARING_CCD_FIELD;
        public static final String PETITIONER_GENDER = OrchestrationConstants.D_8_INFERRED_PETITIONER_GENDER;
        public static final String COSTS_CLAIM_GRANTED = OrchestrationConstants.DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD;
        public static final String JUDGE_COSTS_CLAIM_GRANTED = CcdFields.JUDGE_COSTS_CLAIM_GRANTED;
        public static final String COURT_ID = CcdFields.COURT_NAME;
    }

    public static String getCourtId(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.COURT_ID);
    }

    public static String getHusbandOrWife(Map<String, Object> caseData) {
        Gender petitionerInferredGender = Gender.from(getMandatoryStringValue(caseData, CaseDataKeys.PETITIONER_GENDER));
        Relation petitionerRelationshipToRespondent = Relation.from(petitionerInferredGender);

        return petitionerRelationshipToRespondent.getValue();
    }

    public static boolean isLegalAdvisorCostsClaimGranted(Map<String, Object> caseData) {
        return YES_VALUE.equalsIgnoreCase((String) caseData.get(CaseDataKeys.COSTS_CLAIM_GRANTED));
    }

    public static boolean isCostsClaimGranted(Map<String, Object> caseData, boolean isToggleOn) {
        if (!isToggleOn) {
            return isLegalAdvisorCostsClaimGranted(caseData);
        }

        String judgeDecision = getOptionalPropertyValueAsString(caseData, CaseDataKeys.JUDGE_COSTS_CLAIM_GRANTED, EMPTY_STRING);
        if (isNullOrEmpty(judgeDecision)) {
            return isLegalAdvisorCostsClaimGranted(caseData);
        }
        return YES_VALUE.equalsIgnoreCase(judgeDecision);
    }
}
