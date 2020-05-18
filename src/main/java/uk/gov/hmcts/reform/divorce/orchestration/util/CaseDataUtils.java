package uk.gov.hmcts.reform.divorce.orchestration.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.time.format.DateTimeFormatter.ofPattern;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.CASE_REFERENCE_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.VALUE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_DATE_FORMAT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATETIME_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATE_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_COSTS_ENDCLAIM_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_COSTS_OPTIONS_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.EMPTY_STRING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LANGUAGE_PREFERENCE_WELSH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.REFUSAL_DECISION_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.REFUSAL_REJECTION_ADDITIONAL_INFO;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WELSH_DN_REFUSED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WELSH_REFUSAL_REJECTION_ADDITIONAL_INFO;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsObject;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;

@Slf4j
public class CaseDataUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String formatCaseIdToReferenceNumber(String referenceId) {
        try {
            return String.format("%s-%s-%s-%s",
                referenceId.substring(0, 4),
                referenceId.substring(4, 8),
                referenceId.substring(8, 12),
                referenceId.substring(12));
        } catch (Exception exception) {
            log.warn("Error formatting case reference {}", referenceId);
            return referenceId;
        }
    }

    public static LocalDate getLatestCourtHearingDateFromCaseData(Map<String, Object> caseData) throws TaskException {
        List<CollectionMember> courtHearingCollection = objectMapper.convertValue(
            getMandatoryPropertyValueAsObject(caseData, DATETIME_OF_HEARING_CCD_FIELD), new TypeReference<List<CollectionMember>>() {
            });
        // Last element of list is the latest updated Court Hearing Date
        CollectionMember<Map<String, Object>> hearingDateTime = courtHearingCollection.get(courtHearingCollection.size() - 1);

        return LocalDate.parse(getMandatoryPropertyValueAsString(hearingDateTime.getValue(), DATE_OF_HEARING_CCD_FIELD),
            ofPattern(CCD_DATE_FORMAT));
    }

    public static String getCaseLinkValue(Map<String, Object> caseData, String fieldName) {
        return Optional.ofNullable(getFieldAsStringObjectMap(caseData, fieldName))
            .map(mapData -> mapData.get(CASE_REFERENCE_FIELD))
            .map(String.class::cast)
            .orElse(null);
    }

    public static Map<String, Object> getFieldAsStringObjectMap(Map<String, Object> caseData, String fieldName) {
        return (Map<String, Object>) caseData.get(fieldName);
    }

    public static Map<String, Object> createCaseLinkField(String fieldName, String linkId) {
        return ImmutableMap.of(fieldName, ImmutableMap.of(CASE_REFERENCE_FIELD, linkId));
    }

    public static Map<String, Object> getElementFromCollection(Map<String, Object> collectionEntry) {
        return getFieldAsStringObjectMap(collectionEntry, VALUE_KEY);
    }

    public static boolean isPetitionerClaimingCosts(Map<String, Object> caseData) {
        return YES_VALUE.equalsIgnoreCase(String.valueOf(caseData.get(DIVORCE_COSTS_CLAIM_CCD_FIELD)))
            && !DN_COSTS_ENDCLAIM_VALUE.equalsIgnoreCase(String.valueOf(caseData.get(DN_COSTS_OPTIONS_CCD_FIELD)))
            && Objects.nonNull(caseData.get(DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD));
    }

    public static boolean isRejectReasonAddInfoAwaitingTranslation(Map<String, Object> caseData) {
        String refusalDecision = (String) caseData.getOrDefault(REFUSAL_DECISION_CCD_FIELD, EMPTY_STRING);
        String refusalAdditionalInfo = (String) caseData.getOrDefault(REFUSAL_REJECTION_ADDITIONAL_INFO, EMPTY_STRING);
        String welshRefusalAdditionalInfo = (String) caseData.getOrDefault(WELSH_REFUSAL_REJECTION_ADDITIONAL_INFO, EMPTY_STRING);
        return isLanguagePreferenceWelsh(caseData) && refusalDecision.equals(OrchestrationConstants.DN_REFUSED_REJECT_OPTION) && !refusalAdditionalInfo.isEmpty() && welshRefusalAdditionalInfo.isEmpty();
    }

    public static boolean isWelshTranslationRequiredForDnRefusal(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getCaseData();
        String welshRefusalAdditionalInfo = (String) caseData.getOrDefault(WELSH_REFUSAL_REJECTION_ADDITIONAL_INFO, EMPTY_STRING);
        return WELSH_DN_REFUSED.equals(caseDetails.getState()) && welshRefusalAdditionalInfo.isEmpty();
    }

    public static boolean isLanguagePreferenceWelsh(Map<String, Object> caseData) {
        return (Optional.ofNullable(caseData)
            .map(data -> data.get(LANGUAGE_PREFERENCE_WELSH))
            .filter(Objects::nonNull)
            .map(String.class::cast)
            .filter(YES_VALUE::equalsIgnoreCase)
            .map(languagePreferenceWelsh -> Boolean.TRUE)
            .orElse(Boolean.FALSE));
    }

    public static Optional<LanguagePreference> getLanguagePreference(Map<String, Object> caseData) {
        return Optional.of(Optional.ofNullable(caseData)
            .map(data -> data.get(LANGUAGE_PREFERENCE_WELSH))
            .filter(Objects::nonNull)
            .map(String.class::cast)
            .filter(YES_VALUE::equalsIgnoreCase)
            .map(languagePreferenceWelsh -> LanguagePreference.WELSH)
            .orElse(LanguagePreference.ENGLISH));
    }
}