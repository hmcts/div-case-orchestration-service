package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NEW_JURISDICTION_CONNECTION_POLICY_CCD_DATA;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.OLD_JURISDICTION_CONNECTION_POLICY_CCD_DATA;


@Component
@Slf4j
public class CopyD8JurisdictionConnectionPolicyTask implements Task<Map<String, Object>> {

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {

        /*
         * Solicitor Journey has been updated to use D8JurisdictionConnectionNewPolicy instead of D8JurisdictionConnection due to legal
         * wording change. We couldn't update the wording D8JurisdictionConnection because it would change it for all older cases. Due to frontends
         * still using D8Jurisdiction and DivCommonLib using validation rules to make D8JurisdictionConnection mandatory.
         * Copying D8JurisdictionConnectionNewPolicy into D8JurisdictionConnection means all existing functionality will still work while implementing
         * the legal wording change.
         *
         * Jira Tickets: RPET-664, RPET-694, RPET-701, RPET-702, RPET-758, RPET-780
         */
        if (caseData.get(NEW_JURISDICTION_CONNECTION_POLICY_CCD_DATA) != null) {
            log.info("Updating D8JurisdictionConnection to same values as D8JurisdictionConnectionNewPolicy");
            caseData.put(OLD_JURISDICTION_CONNECTION_POLICY_CCD_DATA, caseData.get(NEW_JURISDICTION_CONNECTION_POLICY_CCD_DATA));
        }

        return caseData;
    }
}
