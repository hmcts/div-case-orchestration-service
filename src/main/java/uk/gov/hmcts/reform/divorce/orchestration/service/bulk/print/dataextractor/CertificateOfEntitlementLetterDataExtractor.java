package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseFieldConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.Gender;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.Relation;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils;

import java.time.LocalDate;
import java.time.Period;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.ExtractorHelper.getMandatoryStringValue;
import static uk.gov.hmcts.reform.divorce.utils.DateUtils.formatDateWithCustomerFacingFormat;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CertificateOfEntitlementLetterDataExtractor {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CaseDataKeys {
        public static final String COURT_NAME = CaseFieldConstants.COURT_NAME;
        public static final String HEARING_DATE = OrchestrationConstants.DATE_OF_HEARING_CCD_FIELD;
        public static final String HEARING_DATE_TIME = OrchestrationConstants.DATETIME_OF_HEARING_CCD_FIELD;
        public static final String IS_COSTS_CLAIM_GRANTED = OrchestrationConstants.DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD;
        public static final String RESPONDENT_ADDRESS = CaseFieldConstants.RESPONDENT_DERIVED_CORRESPONDENCE_ADDRESS;
        public static final String SOLICITOR_ADDRESS = CaseFieldConstants.RESPONDENT_SOLICITOR_DERIVED_CORRESPONDENCE_ADDRESS;
        public static final String PETITIONER_GENDER = OrchestrationConstants.D_8_INFERRED_PETITIONER_GENDER;
        public static final String SOLICITOR_REFERENCE = OrchestrationConstants.D8_RESPONDENT_SOLICITOR_REFERENCE;
        public static final String IS_RESPONDENT_REPRESENTED = OrchestrationConstants.RESP_SOL_REPRESENTED;
        public static final Period PERIOD_FOR_CONTACTING_COURT = OrchestrationConstants.PERIOD_BEFORE_HEARING_DATE_TO_CONTACT_COURT;
    }

    public static String getHearingDate(Map<String, Object> caseData) throws TaskException {
        LocalDate dateOfHearing = CaseDataUtils.getLatestCourtHearingDateFromCaseData(caseData);
        return formatDateWithCustomerFacingFormat(dateOfHearing);
    }

    public static String getLimitDateToContactCourt(Map<String, Object> caseData) throws TaskException {
        LocalDate dateOfHearing = CaseDataUtils.getLatestCourtHearingDateFromCaseData(caseData);
        LocalDate limitDateToContactCourt = dateOfHearing.minus(CaseDataKeys.PERIOD_FOR_CONTACTING_COURT);
        return formatDateWithCustomerFacingFormat(limitDateToContactCourt);
    }

    public static String getHusbandOrWife(Map<String, Object> caseData) {
        Gender petitionerInferredGender = Gender.from(getMandatoryStringValue(caseData, CaseDataKeys.PETITIONER_GENDER));
        Relation petitionerRelationshipToRespondent = Relation.from(petitionerInferredGender);
        return petitionerRelationshipToRespondent.getValue();
    }

    public static String getCourtName(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.COURT_NAME);
    }

    public static boolean isCostsClaimGranted(Map<String, Object> caseData) {
        return YES_VALUE.equalsIgnoreCase((String) caseData.get(CaseDataKeys.IS_COSTS_CLAIM_GRANTED));
    }

    public static String getSolicitorReference(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.SOLICITOR_REFERENCE);
    }
}
