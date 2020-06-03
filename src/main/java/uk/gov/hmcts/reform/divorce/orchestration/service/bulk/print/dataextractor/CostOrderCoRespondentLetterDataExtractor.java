package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.InvalidDataForTaskException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.time.LocalDate;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.StringHelper.buildFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CostOrderCoRespondentLetterDataExtractor {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CaseDataKeys {
        public static final String COSTS_CLAIM_GRANTED = OrchestrationConstants.COSTS_CLAIM_GRANTED;

        public static final String CO_RESPONDENT_FIRST_NAME = OrchestrationConstants.D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_FNAME;
        public static final String CO_RESPONDENT_LAST_NAME = OrchestrationConstants.D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_LNAME;
        public static final String C0_RESPONDENT_ADDRESS = OrchestrationConstants.D8_DERIVED_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_ADDRESS;
    }

    public static boolean isCostClaimGranted(Map<String, Object> caseData) throws TaskException {
        return getMandatoryPropertyValueAsString(caseData, CaseDataKeys.COSTS_CLAIM_GRANTED).equalsIgnoreCase("yes") ? true : false;
    }

    public static Addressee getAddressee(Map<String, Object> caseData) {
        return Addressee.builder()
            .name(getCoRespondentFullName(caseData))
            .formattedAddress(CaseDataKeys.C0_RESPONDENT_ADDRESS)
            .build();
    }

    public static String getLetterDate() {
        return LocalDate.now().toString();
    }

    public static String getCoRespondentFullName(Map<String, Object> caseData) {
        return buildFullName(caseData, CaseDataKeys.CO_RESPONDENT_FIRST_NAME, CaseDataKeys.CO_RESPONDENT_LAST_NAME);
    }

}
