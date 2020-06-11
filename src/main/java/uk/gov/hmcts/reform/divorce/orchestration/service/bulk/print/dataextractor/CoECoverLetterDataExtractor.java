package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.InvalidDataForTaskException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PERIOD_BEFORE_HEARING_DATE_TO_CONTACT_COURT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.ExtractorHelper.getMandatoryStringValue;
import static uk.gov.hmcts.reform.divorce.utils.DateUtils.formatDateWithCustomerFacingFormat;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CoECoverLetterDataExtractor {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CaseDataKeys {
        public static final String COST_CLAIMED = OrchestrationConstants.DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD;
        public static final String COURT_ID = BulkCaseConstants.COURT_NAME_CCD_FIELD;
    }

    public static boolean isCostsClaimGranted(Map<String, Object> caseData) {
        return Optional.ofNullable(caseData.get(CaseDataKeys.COST_CLAIMED))
            .map(String.class::cast)
            .map(YES_VALUE::equalsIgnoreCase)
            .orElse(false);
    }

    public static String getCourtId(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.COURT_ID);
    }

    public static String getHearingDate(Map<String, Object> caseData) {
        return formatDateWithCustomerFacingFormat(getHearingLocalDate(caseData));
    }

    public static String getDeadlineToContactCourtBy(Map<String, Object> caseData) {
        return formatDateWithCustomerFacingFormat(
            getHearingLocalDate(caseData).minus(PERIOD_BEFORE_HEARING_DATE_TO_CONTACT_COURT)
        );
    }

    private static LocalDate getHearingLocalDate(Map<String, Object> caseData) {
        try {
            return CaseDataUtils.getLatestCourtHearingDateFromCaseData(caseData);
        } catch (TaskException e) {
            throw new InvalidDataForTaskException(e);
        }
    }
}
