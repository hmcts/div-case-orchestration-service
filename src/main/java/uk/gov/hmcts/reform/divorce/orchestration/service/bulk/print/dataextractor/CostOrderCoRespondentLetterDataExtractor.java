package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;

import java.time.LocalDate;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.StringHelper.buildFullName;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CostOrderCoRespondentLetterDataExtractor {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CaseDataKeys {
        public static final String COSTS_CLAIM_GRANTED = OrchestrationConstants.COSTS_CLAIM_GRANTED;

        public static final String CO_RESPONDENT_FIRST_NAME = OrchestrationConstants.D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_FNAME;
        public static final String CO_RESPONDENT_LAST_NAME = OrchestrationConstants.D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_LNAME;
    }

    public static String getLetterDate() {
        return LocalDate.now().toString();
    }

    public static String getCoRespondentFullName(Map<String, Object> caseData) {
        return buildFullName(caseData, CaseDataKeys.CO_RESPONDENT_FIRST_NAME, CaseDataKeys.CO_RESPONDENT_LAST_NAME);
    }

}
