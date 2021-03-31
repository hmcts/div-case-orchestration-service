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

        /*
         * Instead of updating petitioner-frontend to juggle two jurisdiction variables we decided to copy the contents of the jurisdictionConnection
         * into the new jurisdictionConnectionNewPolicy. jurisdictionConnectionNewPolicy has new wording, we couldn't simply update
         * jurisdictionConnection because this would be reflected in older cases and for legal reasons older cases are still entitled
         * to the older policies.
         *
         * NEW_LEGAL_CONNECTION_POLICY_DIV_SESSION "NewLegalConnectionPolicy" is used in CCD petition tab, respondent frontend, decree nisi frontend
         * and d8minipetition/d8minidraftpetition documents to toggle between showing old jurisdiction vs new jurisdiction
         *
         * Jira Tickets: RPET-664, RPET-694, RPET-701, RPET-702, RPET-758, RPET-780
         */
        if (caseData.get(OLD_JURISDICTION_CONNECTION_POLICY_DIV_SESSION ) != null) {
            log.info("Setting newLegalConnectionPolicy and copying over jurisdiction connection");
            caseData.put(NEW_LEGAL_CONNECTION_POLICY_DIV_SESSION, YES_VALUE);
            caseData.put(NEW_JURISDICTION_CONNECTION_POLICY_DIV_SESSION, caseData.get(OLD_JURISDICTION_CONNECTION_POLICY_DIV_SESSION));
        }

        return caseData;
    }
}
