package uk.gov.hmcts.reform.divorce.orchestration.tasks.aospack.offline;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_CO_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;

@Component
@Slf4j
public class CoRespondentAosAnswersProcessorTask implements Task<Map<String, Object>> {

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {
        updateReceivedAosFromCoRespondent(caseData, RECEIVED_AOS_FROM_CO_RESP, YES_VALUE);

        String caseId = getCaseId(context);
        log.info("Updating Case data, setting {} to {} for Case ID:",RECEIVED_AOS_FROM_CO_RESP, YES_VALUE, caseId);

        return caseData;
    }

    protected void updateReceivedAosFromCoRespondent(Map<String, Object> caseData, String name, Object value) {
        caseData.put(name, value);
    }
}
