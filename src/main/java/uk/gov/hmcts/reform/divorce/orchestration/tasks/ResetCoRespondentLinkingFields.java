package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESP_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESP_LINKED_TO_CASE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESP_LINKED_TO_CASE_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_CO_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_CO_RESP_DATE;

@Slf4j
@Component
public class ResetCoRespondentLinkingFields implements Task<Map<String, Object>> {

    @Override
    public Map<String, Object> execute(final TaskContext context, final Map<String, Object> caseData) {
        caseData.put(CO_RESP_LINKED_TO_CASE, null);
        caseData.put(CO_RESP_LINKED_TO_CASE_DATE, null);
        caseData.put(CO_RESP_EMAIL_ADDRESS, null);
        caseData.put(RECEIVED_AOS_FROM_CO_RESP, null);
        caseData.put(RECEIVED_AOS_FROM_CO_RESP_DATE, null);
        return caseData;
    }
}
