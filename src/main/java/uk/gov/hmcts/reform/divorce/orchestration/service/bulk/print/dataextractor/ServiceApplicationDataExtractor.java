package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.DivorceServiceApplication;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.ExtractorHelper.getMandatoryStringValue;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getOptionalPropertyValueAsString;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils.getListOfCollectionMembers;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ServiceApplicationDataExtractor {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CaseDataKeys {
        public static final String REFUSAL_REASON = CcdFields.SERVICE_APPLICATION_REFUSAL_REASON;
        public static final String SERVICE_APPLICATION_GRANTED = CcdFields.SERVICE_APPLICATION_GRANTED;
        public static final String SERVICE_APPLICATION_TYPE = CcdFields.SERVICE_APPLICATION_TYPE;
        public static final String SERVICE_APPLICATION_PAYMENT = CcdFields.SERVICE_APPLICATION_PAYMENT;
    }

    public static DivorceServiceApplication getLastServiceApplication(Map<String, Object> caseData) {
        List<Object> serviceApplications = Optional.ofNullable(caseData.get(CcdFields.SERVICE_APPLICATIONS))
            .map(List.class::cast)
            .orElse(new ArrayList<>());

        if (serviceApplications.isEmpty()) {
            return DivorceServiceApplication.builder().build();
        }

        return new ObjectMapper().convertValue(
                serviceApplications.get(serviceApplications.size() - 1),
                new TypeReference<CollectionMember<DivorceServiceApplication>>() {
                })
            .getValue();
    }

    public static String getServiceApplicationRefusalReason(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.REFUSAL_REASON);
    }

    public static String getServiceApplicationRefusalReasonOrEmpty(Map<String, Object> caseData) {
        return getOptionalPropertyValueAsString(caseData, CaseDataKeys.REFUSAL_REASON, "");
    }

    public static String getServiceApplicationGranted(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.SERVICE_APPLICATION_GRANTED);
    }

    public static String getServiceApplicationType(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.SERVICE_APPLICATION_TYPE);
    }

    public static String getServiceApplicationPayment(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, CaseDataKeys.SERVICE_APPLICATION_PAYMENT);
    }

    public static List<CollectionMember<DivorceServiceApplication>> getListOfServiceApplications(Map<String, Object> caseData) {
        return getListOfCollectionMembers(CcdFields.SERVICE_APPLICATIONS, caseData);
    }
}
