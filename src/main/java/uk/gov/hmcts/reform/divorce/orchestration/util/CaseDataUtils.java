package uk.gov.hmcts.reform.divorce.orchestration.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.CASE_REFERENCE_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.VALUE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_DATE_FORMAT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATETIME_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATE_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_COSTS_ENDCLAIM_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_COSTS_OPTIONS_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CO_RESPONDENT_NAMED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CO_RESPONDENT_NAMED_OLD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.ADULTERY;
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

    public boolean isAdulteryCaseWithNamedCoRespondent(Map<String, Object> caseData) {
        final String divorceReason = getOptionalPropertyValueAsString(caseData, D_8_REASON_FOR_DIVORCE, EMPTY);
        final String coRespondentNamed = getOptionalPropertyValueAsString(caseData, D_8_CO_RESPONDENT_NAMED, EMPTY);
        final String coRespondentNamedOld = getOptionalPropertyValueAsString(caseData, D_8_CO_RESPONDENT_NAMED_OLD, EMPTY);

        // we need to ensure older cases can be used before we fixed config in DIV-5068
        return ADULTERY.equals(divorceReason)
            && (YES_VALUE.equalsIgnoreCase(coRespondentNamed) || YES_VALUE.equalsIgnoreCase(coRespondentNamedOld));
    }

}