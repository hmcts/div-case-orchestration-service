package uk.gov.hmcts.reform.divorce.orchestration.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.model.ccd.Document;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.DynamicList;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.utils.DateUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.CASE_REFERENCE_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.VALUE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.WELSH_LA_DECISION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ADDITIONAL_INFRORMATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COSTS_ORDER_ADDITIONAL_INFO;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COSTS_ORDER_ADDITIONAL_INFO_WELSH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8DOCUMENTS_GENERATED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATETIME_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATE_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_COSTS_ENDCLAIM_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_COSTS_OPTIONS_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_REFUSED_REJECT_OPTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CO_RESPONDENT_NAMED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CO_RESPONDENT_NAMED_OLD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.FEE_PAY_BY_ACCOUNT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LANGUAGE_PREFERENCE_WELSH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.REFUSAL_CLARIFICATION_ADDITIONAL_INFO;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.REFUSAL_CLARIFICATION_ADDITIONAL_INFO_WELSH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.REFUSAL_DECISION_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.REFUSAL_DECISION_MORE_INFO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.REFUSAL_REJECTION_ADDITIONAL_INFO;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.REFUSAL_REJECTION_ADDITIONAL_INFO_WELSH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.TYPE_COSTS_DECISION_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFact.ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.SolicitorDataExtractor.getPaymentMethod;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsObject;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getOptionalPropertyValueAsString;

@Slf4j
@Component
public class CaseDataUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String MALE_GENDER = "male";
    private static final String FEMALE_GENDER = "female";
    private static final String MALE_GENDER_IN_RELATION = "husband";
    private static final String FEMALE_GENDER_IN_RELATION = "wife";

    public static String getRelationshipTermByGender(final String gender) {
        if (gender == null) {
            return null;
        }

        switch (gender.toLowerCase(Locale.ENGLISH)) {
            case MALE_GENDER:
                return MALE_GENDER_IN_RELATION;
            case FEMALE_GENDER:
                return FEMALE_GENDER_IN_RELATION;
            default:
                return null;
        }
    }

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
        List<CollectionMember<Map<String, Object>>> courtHearingCollection = objectMapper.convertValue(
            getMandatoryPropertyValueAsObject(caseData, DATETIME_OF_HEARING_CCD_FIELD),
            new TypeReference<List<CollectionMember<Map<String, Object>>>>() {
            }
        );

        // Last element of list is the latest updated Court Hearing Date
        CollectionMember<Map<String, Object>> hearingDateTime = courtHearingCollection.get(courtHearingCollection.size() - 1);

        return LocalDate.parse(
            getMandatoryPropertyValueAsString(hearingDateTime.getValue(), DATE_OF_HEARING_CCD_FIELD),
            DateUtils.Formatters.CCD_DATE
        );
    }

    public static String getCaseLinkValue(Map<String, Object> caseData, String fieldName) {
        return Optional.ofNullable(getFieldAsStringObjectMap(caseData, fieldName))
            .map(mapData -> mapData.get(CASE_REFERENCE_FIELD))
            .map(String.class::cast)
            .orElse(null);
    }

    public static Map<String, Object> getFieldAsStringObjectMap(Map<String, ?> caseData, String fieldName) {
        return (Map<String, Object>) caseData.get(fieldName);
    }

    public static Map<String, Object> createCaseLinkField(String fieldName, String linkId) {
        return ImmutableMap.of(fieldName, ImmutableMap.of(CASE_REFERENCE_FIELD, linkId));
    }

    public static boolean isPetitionerClaimingCosts(Map<String, Object> caseData) {
        return YES_VALUE.equalsIgnoreCase(String.valueOf(caseData.get(DIVORCE_COSTS_CLAIM_CCD_FIELD)))
            && !DN_COSTS_ENDCLAIM_VALUE.equalsIgnoreCase(String.valueOf(caseData.get(DN_COSTS_OPTIONS_CCD_FIELD)))
            && Objects.nonNull(caseData.get(DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD));
    }

    public boolean isAdulteryCaseWithNamedCoRespondent(Map<String, Object> caseData) {
        final String divorceReason = getOptionalPropertyValueAsString(caseData, D_8_REASON_FOR_DIVORCE, EMPTY);
        final String coRespondentNamed = getOptionalPropertyValueAsString(caseData, D_8_CO_RESPONDENT_NAMED, EMPTY);
        final String coRespondentNamedOld = getOptionalPropertyValueAsString(caseData, D_8_CO_RESPONDENT_NAMED_OLD, EMPTY);

        // we need to ensure older cases can be used before we fixed config in DIV-5068
        return ADULTERY.getValue().equals(divorceReason)
            && (YES_VALUE.equalsIgnoreCase(coRespondentNamed) || YES_VALUE.equalsIgnoreCase(coRespondentNamedOld));
    }

    public static boolean isRejectReasonAddInfoAwaitingTranslation(Map<String, Object> caseData) {
        return Optional.of(isLanguagePreferenceWelsh(caseData)).filter(Boolean::booleanValue)
            .map(yes -> {
                boolean isRefusalReject = Optional.ofNullable(caseData.get(REFUSAL_DECISION_CCD_FIELD))
                    .map(String.class::cast).filter(value -> value.equals(DN_REFUSED_REJECT_OPTION)).isPresent();
                boolean isAwaitingClarification = Optional.ofNullable(caseData.get(REFUSAL_DECISION_CCD_FIELD))
                    .map(String.class::cast).filter(value -> value.equals(REFUSAL_DECISION_MORE_INFO_VALUE)).isPresent();
                boolean isRefusalRejectionStop = shouldStopValidation(caseData,
                    REFUSAL_REJECTION_ADDITIONAL_INFO, REFUSAL_REJECTION_ADDITIONAL_INFO_WELSH);
                boolean isRefusalClarificationStop = shouldStopValidation(caseData,
                    REFUSAL_CLARIFICATION_ADDITIONAL_INFO, REFUSAL_CLARIFICATION_ADDITIONAL_INFO_WELSH);
                boolean isCostClaimGranted = Optional.ofNullable(caseData.get(DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD))
                    .map(String.class::cast).filter(value -> value.equals(YES_VALUE)).isPresent();
                boolean isTypeCostDecision = Optional.ofNullable(caseData.get(TYPE_COSTS_DECISION_CCD_FIELD))
                    .map(String.class::cast).filter(value -> value.equals(ADDITIONAL_INFRORMATION)).isPresent();
                boolean isCostOrderStop = shouldStopValidation(caseData, COSTS_ORDER_ADDITIONAL_INFO, COSTS_ORDER_ADDITIONAL_INFO_WELSH);
                return ((isRefusalReject && isRefusalRejectionStop) || (isAwaitingClarification
                    && isRefusalClarificationStop) || (isCostClaimGranted && isTypeCostDecision && isCostOrderStop));
            })
            .orElse(false);
    }

    private static boolean shouldStopValidation(Map<String, Object> caseData, String englishField, String welshField) {
        boolean englishPresent = Optional.ofNullable(caseData.get(englishField))
            .filter(Objects::nonNull).map(String.class::cast).map(String::trim).filter(value -> !value.isEmpty()).isPresent();
        boolean welshPresent = Optional.ofNullable(caseData.get(welshField))
            .filter(Objects::nonNull).map(String.class::cast).map(String::trim).filter(value -> !value.isEmpty()).isPresent();
        return englishPresent && !welshPresent;
    }

    public static boolean isWelshLADecisionTranslationRequired(CaseDetails caseDetails) {
        return WELSH_LA_DECISION.equals(caseDetails.getState());
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

    public static LanguagePreference getLanguagePreference(Map<String, Object> caseData) {
        boolean preferredLanguageIsWelsh = Optional.ofNullable(caseData)
            .map(data -> data.get(LANGUAGE_PREFERENCE_WELSH))
            .map(String.class::cast)
            .map(YES_VALUE::equalsIgnoreCase)
            .orElse(false);

        return preferredLanguageIsWelsh ? LanguagePreference.WELSH : LanguagePreference.ENGLISH;
    }

    public static Map<String, Object> removeDocumentsByDocumentType(Map<String, Object> caseData, String... documentTypes) {
        List<?> generatedDocuments = Optional.ofNullable(caseData.get(D8DOCUMENTS_GENERATED))
            .map(List.class::cast)
            .orElse(new ArrayList<>());

        Map<String, Object> newCaseData = new HashMap<>(caseData);
        List<String> docTypesList = Arrays.asList(documentTypes);

        if (!generatedDocuments.isEmpty()) {
            List<?> filteredDocumentsList = generatedDocuments.stream()
                .filter(item -> {
                    CollectionMember<Document> document = objectMapper.convertValue(item, new TypeReference<CollectionMember<Document>>() {
                    });
                    return !docTypesList.contains(document.getValue().getDocumentType());
                })
                .collect(Collectors.toList());

            if (filteredDocumentsList.isEmpty()) {
                newCaseData.remove(D8DOCUMENTS_GENERATED);
            } else {
                newCaseData.replace(D8DOCUMENTS_GENERATED, filteredDocumentsList);
            }

        }

        return newCaseData;
    }

    public static Map<String, Object> getElementFromCollection(Map<String, ?> collectionEntry) {
        return getFieldAsStringObjectMap(collectionEntry, VALUE_KEY);
    }

    public static boolean isSolicitorPaymentMethodPba(Map<String, Object> caseData) {
        return Optional.of(getPaymentMethod(caseData))
            .map(i -> i.equals(FEE_PAY_BY_ACCOUNT))
            .orElse(false);
    }

    public static DynamicList getAsDynamicList(Map<String, Object> caseData, String key) {
        return objectMapper.convertValue(getMandatoryPropertyValueAsObject(caseData, key), DynamicList.class);
    }

    public static <T> List<CollectionMember<T>> getListOfCollectionMembers(String field, Map<String, Object> caseData) {
        List<CollectionMember<T>> listOfCollectionMembers;

        Optional<Object> optionalCaseField = Optional.ofNullable(caseData.get(field));

        if (optionalCaseField.isPresent()) {
            listOfCollectionMembers = objectMapper.convertValue(optionalCaseField.get(), new TypeReference<>() {
            });
        } else {
            listOfCollectionMembers = new ArrayList<>();
        }

        return listOfCollectionMembers;
    }

}