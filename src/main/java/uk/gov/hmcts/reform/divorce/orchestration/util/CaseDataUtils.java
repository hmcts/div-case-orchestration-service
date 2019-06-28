package uk.gov.hmcts.reform.divorce.orchestration.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.time.format.DateTimeFormatter.ofPattern;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_DATE_FORMAT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATETIME_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATE_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsObject;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;

@Slf4j
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
                getMandatoryPropertyValueAsObject(caseData, DATETIME_OF_HEARING_CCD_FIELD), new TypeReference<List<CollectionMember>>() {});
        // Last element of list is the latest updated Court Hearing Date
        CollectionMember<Map<String, Object>> hearingDateTime = courtHearingCollection.get(courtHearingCollection.size() - 1);

        return LocalDate.parse(getMandatoryPropertyValueAsString(hearingDateTime.getValue(), DATE_OF_HEARING_CCD_FIELD),
                ofPattern(CCD_DATE_FORMAT));
    }
}