package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getOptionalPropertyValueAsString;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BailiffServiceApplicationDataExtractor {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CaseDataKeys {
        public static final String LOCAL_COURT_ADDRESS = CcdFields.LOCAL_COURT_ADDRESS;
        public static final String LOCAL_COURT_EMAIL = CcdFields.LOCAL_COURT_EMAIL;
        public static final String CERTIFICATE_OF_SERVICE_DATE = CcdFields.CERTIFICATE_OF_SERVICE_DATE;
        public static final String BAILIFF_SERVICE_SUCCESSFUL = CcdFields.BAILIFF_SERVICE_SUCCESSFUL;
        public static final String REASON_FAILURE_TO_SERVE = CcdFields.REASON_FAILURE_TO_SERVE;
        public static final String BAILIFF_APPLICATION_GRANTED = CcdFields.BAILIFF_APPLICATION_GRANTED;
    }

    public static String getLocalCourtAddress(Map<String, Object> caseData) {
        return getOptional(caseData, CaseDataKeys.LOCAL_COURT_ADDRESS);
    }

    public static String getLocalCourtEmail(Map<String, Object> caseData) {
        return getOptional(caseData, CaseDataKeys.LOCAL_COURT_EMAIL);
    }

    public static String getCertificateOfServiceDate(Map<String, Object> caseData) {
        return getOptional(caseData, CaseDataKeys.CERTIFICATE_OF_SERVICE_DATE);
    }

    public static String getBailiffServiceSuccessful(Map<String, Object> caseData) {
        return getOptional(caseData, CaseDataKeys.BAILIFF_SERVICE_SUCCESSFUL);
    }

    public static String getBailiffApplicationGranted(Map<String, Object> caseData) {
        return getOptional(caseData, CaseDataKeys.BAILIFF_APPLICATION_GRANTED);
    }

    public static String getReasonFailureToServe(Map<String, Object> caseData) {
        return getOptional(caseData, CaseDataKeys.REASON_FAILURE_TO_SERVE);
    }

    private static String getOptional(Map<String, Object> caseData, String field) {
        return getOptionalPropertyValueAsString(caseData, field, "");
    }
}
