package uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextractor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.Gender;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.Relation;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CoECoverLetterDataExtractor extends Extractable {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CaseDataKeys {
        public static final String COURT_NAME = BulkCaseConstants.COURT_NAME_CCD_FIELD;
        public static final String HEARING_DATE = OrchestrationConstants.DATE_OF_HEARING_CCD_FIELD;
        public static final String HEARING_DATE_TIME = OrchestrationConstants.DATETIME_OF_HEARING_CCD_FIELD;
        public static final String PETITIONER_GENDER = OrchestrationConstants.D_8_INFERRED_PETITIONER_GENDER;
        public static final String COSTS_CLAIM_GRANTED = OrchestrationConstants.DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD;
        public static final String COURT_ID = BulkCaseConstants.COURT_NAME_CCD_FIELD;
    }

    public static String getCourtId(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.COURT_ID);
    }

    public static String getHusbandOrWife(Map<String, Object> caseData) {
        Gender petitionerInferredGender = Gender.from(getMandatoryStringValue(caseData, CaseDataKeys.PETITIONER_GENDER));
        Relation petitionerRelationshipToRespondent = Relation.from(petitionerInferredGender);

        return petitionerRelationshipToRespondent.getValue();
    }

    public static boolean isCostsClaimGranted(Map<String, Object> caseData) {
        return YES_VALUE.equalsIgnoreCase((String) caseData.get(CaseDataKeys.COSTS_CLAIM_GRANTED));
    }
}
