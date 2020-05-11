package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;

import static com.google.common.base.Strings.nullToEmpty;
import static java.lang.String.format;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_FILENAME_FMT;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StringHelper {

    public static String formatFilename(String caseId, String filename) {
        return format(DOCUMENT_FILENAME_FMT, notNull(filename), notNull(caseId));
    }

    public static String buildFullName(Map<String, Object> caseData, String firstName, String lastName) {
        return (
            nullToEmpty((String) (caseData.get(firstName))).trim()
                + " "
                + nullToEmpty((String) (caseData.get(lastName))).trim()
        ).trim();
    }

    public static String notNull(String value) {
        if (null == value) {
            throw new IllegalArgumentException("Value must not be null");
        }

        return value;
    }
}
