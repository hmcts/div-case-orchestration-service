package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.BailiffServiceApplication;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.BailiffServiceApplicationDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DatesDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.ServiceApplicationDataExtractor;

import java.util.Map;

@Component
public class BailiffServiceApplicationDataTask extends ServiceApplicationDataTask {

    public BailiffServiceApplicationDataTask(FeatureToggleService featureToggleService) {
        super(featureToggleService);
    }

    @Override
    protected BailiffServiceApplication buildServiceApplication(Map<String, Object> caseData) {
        return BailiffServiceApplication.bailiffServiceApplicationBuilder()
            .addedDate(DatesDataExtractor.getReceivedServiceAddedDateUnformatted(caseData))
            .receivedDate(DatesDataExtractor.getReceivedServiceApplicationDateUnformatted(caseData))
            .type(ServiceApplicationDataExtractor.getServiceApplicationType(caseData))
            .applicationGranted(ServiceApplicationDataExtractor.getServiceApplicationGranted(caseData))
            .bailiffApplicationGranted(BailiffServiceApplicationDataExtractor.getBailiffApplicationGranted(caseData))
            .decisionDate(DatesDataExtractor.getServiceApplicationDecisionDateUnformatted(caseData))
            .payment(ServiceApplicationDataExtractor.getServiceApplicationPayment(caseData))
            .refusalReason(ServiceApplicationDataExtractor.getServiceApplicationRefusalReasonOrEmpty(caseData))
            .localCourtAddress(BailiffServiceApplicationDataExtractor.getLocalCourtAddress(caseData))
            .localCourtEmail(BailiffServiceApplicationDataExtractor.getLocalCourtEmail(caseData))
            .certificateOfServiceDate(BailiffServiceApplicationDataExtractor.getCertificateOfServiceDate(caseData))
            .successfulServedByBailiff(BailiffServiceApplicationDataExtractor.getBailiffServiceSuccessful(caseData))
            .reasonFailureToServe(BailiffServiceApplicationDataExtractor.getReasonFailureToServe(caseData))
            .build();
    }
}
