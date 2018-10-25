package uk.gov.hmcts.reform.divorce.orchestration.util;

import lombok.extern.slf4j.Slf4j;

import java.util.Locale;

@Slf4j
public class CaseDataUtils {

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

}