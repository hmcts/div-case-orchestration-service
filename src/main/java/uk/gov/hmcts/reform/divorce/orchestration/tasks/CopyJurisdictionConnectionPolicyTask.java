package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NEW_JURISDICTION_CONNECTION_POLICY_DIV_SESSION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NEW_LEGAL_CONNECTION_POLICY_DIV_SESSION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.OLD_JURISDICTION_CONNECTION_POLICY_DIV_SESSION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;


@Component
@Slf4j
public class CopyJurisdictionConnectionPolicyTask implements Task<Map<String, Object>> {

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {

        if (caseData.get(OLD_JURISDICTION_CONNECTION_POLICY_DIV_SESSION ) != null) {
            log.info("Setting newLegalConnectionPolicy and copying over jurisdiction connection");
            caseData.put(NEW_LEGAL_CONNECTION_POLICY_DIV_SESSION, YES_VALUE);
            caseData.put(NEW_JURISDICTION_CONNECTION_POLICY_DIV_SESSION, caseData.get(OLD_JURISDICTION_CONNECTION_POLICY_DIV_SESSION));
        }

        return caseData;
    }
}
