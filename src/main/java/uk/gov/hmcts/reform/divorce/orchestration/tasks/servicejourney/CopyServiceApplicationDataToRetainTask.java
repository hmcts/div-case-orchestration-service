package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.ServiceApplicationDataExtractor;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;

@Component
@Slf4j
@AllArgsConstructor
public class CopyServiceApplicationDataToRetainTask implements Task<Map<String, Object>> {

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        String caseId = getCaseId(context);
        log.info("CaseId: {} moving temp service application data to retain to new fields.", caseId);

        caseData.put(CcdFields.LAST_SERVICE_APPLICATION_GRANTED, ServiceApplicationDataExtractor.getServiceApplicationGranted(caseData));
        caseData.put(CcdFields.LAST_SERVICE_APPLICATION_TYPE, ServiceApplicationDataExtractor.getServiceApplicationType(caseData));

        return caseData;
    }
}
