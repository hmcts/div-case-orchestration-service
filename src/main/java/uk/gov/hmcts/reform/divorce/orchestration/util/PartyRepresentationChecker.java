package uk.gov.hmcts.reform.divorce.orchestration.util;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;

import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.CO_RESPONDENT_LINKED_TO_CASE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_EMAIL_PARTIES;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.PETITIONER_SOLICITOR_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.RESPONDENT_SOLICITOR_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_IS_USING_DIGITAL_CHANNEL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_RESPONDENT_SOLICITOR_COMPANY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_RESPONDENT_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_SESSION_RESPONDENT_SOLICITOR_REFERENCE_DATA_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.EMPTY_STRING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_IS_USING_DIGITAL_CHANNEL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_SOL_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WHO_PAYS_CCD_CODE_FOR_BOTH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WHO_PAYS_CCD_CODE_FOR_CO_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WHO_PAYS_COSTS_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.EmailDataExtractor.CaseDataKeys.OTHER_PARTY_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.OTHER_PARTY_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.SolicitorDataExtractor.getSolicitorOrganisationPolicy;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.OrganisationPolicyHelper.isOrganisationPolicyPopulated;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getOptionalPropertyValueAsString;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PartyRepresentationChecker {

    public static boolean isPetitionerRepresented(Map<String, Object> caseData) {
        String petitionerSolicitorEmail = (String) caseData.get(PETITIONER_SOLICITOR_EMAIL);

        return !Strings.isNullOrEmpty(petitionerSolicitorEmail);
    }

    public static boolean isRespondentRepresented(Map<String, Object> caseData) {
        return isRepresented(caseData, RESP_SOL_REPRESENTED);
    }

    // all methods in this class must be static - yuch
    public static boolean usingRespondentSolicitor(Map<String, Object> caseData) {
        // temporary fix until we implement setting respondentSolicitorRepresented from CCD for RespSols
        return isRespondentRepresented(caseData) || hasRespondentSolicitorDetail(caseData);
    }

    public static boolean isCoRespondentLinkedToCase(Map<String, Object> caseData) {
        return isYesOnly(caseData, CO_RESPONDENT_LINKED_TO_CASE);
    }

    public static boolean isCoRespondentRepresented(Map<String, Object> caseData) {
        return isRepresented(caseData, CO_RESPONDENT_REPRESENTED);
    }

    public static boolean isRespondentDigital(Map<String, Object> caseData) {
        return isYesOrEmpty(caseData, RESP_IS_USING_DIGITAL_CHANNEL);
    }

    public static boolean isPetitionerSolicitorDigital(Map<String, Object> caseData) {
        return isPopulatedOrganisation(caseData, PETITIONER_SOLICITOR_ORGANISATION_POLICY);
    }

    /**
     * This is meant to be used after case is submitted.
     *
     * For cases that are not submitted yet we should use isRespondentSolicitorDigitalSelectedYes
     */
    public static boolean isRespondentSolicitorDigital(Map<String, Object> caseData) {
        return isPopulatedOrganisation(caseData, RESPONDENT_SOLICITOR_ORGANISATION_POLICY);
    }

    /**
     * This is meant to be used before case is submitted to cover edge case DIV-6869.
     *
     * For cases already submitted (in exui / PFE) we should use isRespondentSolicitorDigital
     */
    public static boolean isRespondentSolicitorDigitalSelectedYes(Map<String, Object> caseData) {
        return getOptionalPropertyValueAsString(caseData, CcdFields.RESPONDENT_SOLICITOR_DIGITAL, EMPTY_STRING)
            .equalsIgnoreCase(YES_VALUE);
    }

    public static boolean isCoRespondentDigital(Map<String, Object> caseData) {
        return isYesOrEmpty(caseData, CO_RESPONDENT_IS_USING_DIGITAL_CHANNEL);
    }

    public static boolean isCoRespondentLiableForCosts(Map<String, Object> caseData) {
        String whoPaysCosts = String.valueOf(caseData.get(WHO_PAYS_COSTS_CCD_FIELD));

        return WHO_PAYS_CCD_CODE_FOR_CO_RESPONDENT.equalsIgnoreCase(whoPaysCosts)
            || WHO_PAYS_CCD_CODE_FOR_BOTH.equalsIgnoreCase(whoPaysCosts);
    }

    public static boolean isOtherPartyDigital(Map<String, Object> caseData) {
        String otherPartyEmail = (String) caseData.get(OTHER_PARTY_EMAIL);
        String otherPartyName = (String) caseData.get(OTHER_PARTY_NAME);

        return (!Strings.isNullOrEmpty(otherPartyEmail) && !Strings.isNullOrEmpty(otherPartyName));
    }

    public static boolean isRespondentSolicitorDigitalDivorceSession(Map<String, Object> divorceSession) {
        String respondentSolicitorReferenceDataId = (String) divorceSession.get(DIVORCE_SESSION_RESPONDENT_SOLICITOR_REFERENCE_DATA_ID);

        return !Strings.isNullOrEmpty(respondentSolicitorReferenceDataId);
    }

    public static String getGeneralEmailParties(Map<String, Object> caseData) {
        String party = (String) caseData.get(GENERAL_EMAIL_PARTIES);

        if (!Strings.isNullOrEmpty(party)) {
            return party;
        }

        return null;
    }

    private static boolean isRepresented(Map<String, Object> caseData, String field) {
        return isYesOnly(caseData, field);
    }

    private static boolean isYesOrEmpty(Map<String, Object> caseData, String field) {
        String value = (String) caseData.get(field);
        return Strings.isNullOrEmpty(value) || YES_VALUE.equalsIgnoreCase(value);
    }

    private static boolean isYesOnly(Map<String, Object> caseData, String field) {
        return YES_VALUE.equalsIgnoreCase((String) caseData.get(field));
    }

    private static boolean isPopulatedOrganisation(Map<String, Object> caseData, String field) {
        return isOrganisationPolicyPopulated(getSolicitorOrganisationPolicy(caseData, field));
    }

    private static boolean hasRespondentSolicitorDetail(Map<String, Object> caseData) {
        return isNotEmpty(getOptionalPropertyValueAsString(caseData, D8_RESPONDENT_SOLICITOR_NAME, EMPTY_STRING))
            && isNotEmpty(getOptionalPropertyValueAsString(caseData, D8_RESPONDENT_SOLICITOR_COMPANY, EMPTY_STRING));
    }

}
