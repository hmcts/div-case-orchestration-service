package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_RESP_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_EMAIL_ADDRESS;

@Slf4j
@Component
public class ResetRespondentLinkingFields implements Task<Map<String, Object>> {

    @Override
    public Map<String, Object> execute(final TaskContext context, final Map<String, Object> caseData) {
        caseData.put(RECEIVED_AOS_FROM_RESP, null);
        caseData.put(RECEIVED_AOS_FROM_RESP_DATE, null);
        caseData.put(RESPONDENT_EMAIL_ADDRESS, null);
        return caseData;
    }
}
