package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NEW_LEGAL_CONNECTION_POLICY_CCD_DATA;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@Component
@Slf4j
public class SetNewLegalConnectionPolicyTask implements Task<Map<String, Object>> {

    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        /*
         * NEW_LEGAL_CONNECTION_POLICY_DIV_SESSION "NewLegalConnectionPolicy" is used in CCD petition tab, respondent frontend,
         * decree nisi frontend and d8minipetition/d8minidraftpetition documents to toggle between showing
         * old jurisdiction vs new jurisdiction wording
         *
         * Jira Tickets: RPET-664, RPET-694, RPET-701, RPET-702, RPET-758, RPET-780
         */
        log.info("Setting NewLegalConnectionPolicy to {}", YES_VALUE);
        caseData.put(NEW_LEGAL_CONNECTION_POLICY_CCD_DATA, YES_VALUE);

        return caseData;
    }
}
