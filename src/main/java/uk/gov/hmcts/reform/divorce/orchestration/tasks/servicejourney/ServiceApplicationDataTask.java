package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.DivorceServiceApplication;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DatesDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.ServiceApplicationDataExtractor;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;

@Component
@Slf4j
@AllArgsConstructor
public class ServiceApplicationDataTask implements Task<Map<String, Object>> {

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        String caseId = getCaseId(context);
        log.info("CaseId: {} moving temp service application data to collection.", caseId);

        DivorceServiceApplication serviceApplication = buildServiceApplication(caseData);

        persistLastServiceApplication(caseData, serviceApplication);
        return addNewServiceApplicationToCaseData(caseData, serviceApplication);
    }

    private void persistLastServiceApplication(Map<String, Object> caseData, DivorceServiceApplication serviceApplication) {
        caseData.put(CcdFields.LAST_SERVICE_APPLICATION, serviceApplication);
    }

    private Map<String, Object> addNewServiceApplicationToCaseData(
        Map<String, Object> caseData, DivorceServiceApplication serviceApplication) {

        List<CollectionMember<DivorceServiceApplication>> collection = ServiceApplicationDataExtractor.getListOfServiceApplications(caseData);
        CollectionMember<DivorceServiceApplication> collectionMember = new CollectionMember<>();
        collectionMember.setValue(serviceApplication);

        collection.add(collectionMember);

        caseData.put(CcdFields.SERVICE_APPLICATIONS, collection);

        return caseData;
    }

    private DivorceServiceApplication buildServiceApplication(Map<String, Object> caseData) {
        return DivorceServiceApplication.builder()
            .addedDate(DatesDataExtractor.getReceivedServiceAddedDateUnformatted(caseData))
            .receivedDate(DatesDataExtractor.getReceivedServiceApplicationDateUnformatted(caseData))
            .type(ServiceApplicationDataExtractor.getServiceApplicationType(caseData))
            .applicationGranted(ServiceApplicationDataExtractor.getServiceApplicationGranted(caseData))
            .decisionDate(DatesDataExtractor.getServiceApplicationDecisionDateUnformatted(caseData))
            .payment(ServiceApplicationDataExtractor.getServiceApplicationPayment(caseData))
            .refusalReason(ServiceApplicationDataExtractor.getServiceApplicationRefusalReasonOrEmpty(caseData))
            .build();
    }
}
