package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.DivorceServiceApplication;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DatesDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.util.ServiceApplicationRefusalHelper;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;

@Component
@Slf4j
public class ServiceApplicationDataTask implements Task<Map<String, Object>> {

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {
        String caseId = getCaseId(context);
        log.info("CaseId: {} moving temp service application data to collection.", caseId);

        DivorceServiceApplication serviceApplication = buildServiceApplication(caseData);

        return addNewServiceApplicationToCaseData(caseData, serviceApplication);
    }

    private Map<String, Object> addNewServiceApplicationToCaseData(
        Map<String, Object> caseData, DivorceServiceApplication serviceApplication) {

        // add `serviceApplication` to collection
        // delete temp data

        return caseData;
    }

    private DivorceServiceApplication buildServiceApplication(Map<String, Object> caseData) {
        return DivorceServiceApplication.builder()
            .addedDate(DatesDataExtractor.getReceivedServiceAddedDate(caseData))
            .receivedDate(DatesDataExtractor.getReceivedServiceApplicationDate(caseData))
            .type(ServiceApplicationRefusalHelper.getServiceApplicationType(caseData))
            .applicationGranted(ServiceApplicationRefusalHelper.getServiceApplicationGranted(caseData))
            .decisionDate(DatesDataExtractor.getServiceApplicationDecisionDate(caseData))
            .refusalReason(ServiceApplicationRefusalHelper.getServiceApplicationRefusalReasonOrEmpty(caseData))
            .build();
    }
}
