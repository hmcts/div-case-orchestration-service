package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getCoRespondentFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getCoRespondentSolicitorFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getRespondentFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getRespondentSolicitorFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.ExtractorHelper.getMandatoryStringValue;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AddresseeDataExtractor {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CaseDataKeys {
        public static final String RESPONDENT_ADDRESS = "D8DerivedRespondentCorrespondenceAddr";
        public static final String RESPONDENT_SOLICITOR_ADDRESS = "D8DerivedRespondentSolicitorAddr";
        public static final String CO_RESPONDENT_ADDRESS = "D8DerivedReasonForDivorceAdultery3rdAddr";
        public static final String CO_RESPONDENT_SOLICITOR_ADDRESS = "DerivedCoRespondentSolicitorAddr";
    }

    public static Addressee getRespondent(Map<String, Object> caseData) {
        return Addressee.builder()
            .name(getRespondentFullName(caseData))
            .formattedAddress(getRespondentFormattedAddress(caseData))
            .build();
    }

    public static Addressee getCoRespondent(Map<String, Object> caseData) {
        return Addressee.builder()
            .name(getCoRespondentFullName(caseData))
            .formattedAddress(getCoRespondentFormattedAddress(caseData))
            .build();
    }

    public static Addressee getRespondentSolicitor(Map<String, Object> caseData) {
        return Addressee.builder()
            .name(getRespondentSolicitorFullName(caseData))
            .formattedAddress(getRespondentSolicitorFormattedAddress(caseData))
            .build();
    }

    public static Addressee getCoRespondentSolicitor(Map<String, Object> caseData) {
        return Addressee.builder()
            .name(getCoRespondentSolicitorFullName(caseData))
            .formattedAddress(getCoRespondentSolicitorFormattedAddress(caseData))
            .build();
    }

    private static String getRespondentFormattedAddress(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.RESPONDENT_ADDRESS);
    }

    private static String getCoRespondentFormattedAddress(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.CO_RESPONDENT_ADDRESS);
    }

    private static String getRespondentSolicitorFormattedAddress(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.RESPONDENT_SOLICITOR_ADDRESS);
    }

    private static String getCoRespondentSolicitorFormattedAddress(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.CO_RESPONDENT_SOLICITOR_ADDRESS);
    }
}
