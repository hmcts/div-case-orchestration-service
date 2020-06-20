package uk.gov.hmcts.reform.divorce.orchestration.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.bsp.common.utils.LetterAddressHelper;

import java.util.Map;
import java.util.Optional;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AosPackOfflineDerivedAddressFormatterHelper {

    public static final String CO_RESPONDENT_SOLICITOR_ADDRESS = "CoRespondentSolicitorAddress";
    public static final String D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_ADDRESS = "D8ReasonForDivorceAdultery3rdAddress";
    public static final String D8_RESPONDENT_SOLICITOR_ADDRESS = "D8RespondentSolicitorAddress";
    public static final String D8_DERIVED_RESPONDENT_SOLICITOR_ADDRESS = "D8DerivedRespondentSolicitorAddr";
    public static final String D8_RESPONDENT_HOME_ADDRESS = "D8RespondentHomeAddress";
    public static final String D8_DERIVED_RESPONDENT_HOME_ADDRESS = "D8DerivedRespondentHomeAddress";
    public static final String D8_RESPONDENT_CORRESPONDENCE_ADDRESS = "D8RespondentCorrespondenceAddress";
    public static final String D8_DERIVED_RESPONDENT_CORRESPONDENCE_ADDRESS = "D8DerivedRespondentCorrespondenceAddr";
    public static final String D8_RESPONDENT_CORRESPONDENCE_USE_HOME_ADDRESS = "D8RespondentCorrespondenceUseHomeAddress";

    public static String formatDerivedCoRespondentSolicitorAddress(Map<String, Object> caseData) {
        return formatToDerivedAddress(caseData, CO_RESPONDENT_SOLICITOR_ADDRESS);
    }

    public static String formatDerivedReasonForDivorceAdultery3rdAddress(Map<String, Object> caseData) {
        return formatToDerivedAddress(caseData, D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_ADDRESS);
    }

    public static String formatDerivedRespondentSolicitorAddress(Map<String, Object> caseData) {
        return formatToDerivedAddress(caseData, D8_RESPONDENT_SOLICITOR_ADDRESS);
    }

    public static String formatDerivedRespondentHomeAddress(Map<String, Object> caseData) {
        return formatToDerivedAddress(caseData, D8_RESPONDENT_HOME_ADDRESS);
    }

    public static String formatDerivedRespondentCorrespondenceAddress(Map<String, Object> caseData) {
        String correspondenceAddress = formatToDerivedAddress(caseData, D8_RESPONDENT_CORRESPONDENCE_ADDRESS);

        if (StringUtils.isEmpty(correspondenceAddress)) {
            correspondenceAddress = formatDerivedRespondentHomeAddress(caseData);
        }

        return correspondenceAddress;
    }

    public static String formatToDerivedAddress(Map<String, Object> caseData, String addressType) {
        return Optional.ofNullable((Map<String, Object>) caseData.get(addressType))
            .map(LetterAddressHelper::formatAddressForLetterPrinting)
            .orElse(null);
    }

    public static boolean isRespondentCorrespondenceAddressPopulated(Map<String, Object> caseData) {
        return !StringUtils.isEmpty(formatToDerivedAddress(caseData, D8_RESPONDENT_CORRESPONDENCE_ADDRESS)) ? true : false;
    }
}
