package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.FEE_PAY_BY_ACCOUNT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_HOW_TO_PAY_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;

@Component
@Slf4j
public class MigrateChequeTask implements Task<Map<String, Object>> {

    @Override
    public Map<String, Object> execute(TaskContext context,  Map<String, Object> caseData) {

        log.info("DEBUG Payment Value (should be 'cheque'): {}", caseData.get(SOLICITOR_HOW_TO_PAY_JSON_KEY));

        caseData.replace(SOLICITOR_HOW_TO_PAY_JSON_KEY, FEE_PAY_BY_ACCOUNT);

        log.info("DEBUG Payment Value (should be 'feePayByAccount'): {}", caseData.get(SOLICITOR_HOW_TO_PAY_JSON_KEY));
        log.info("Case id {}: Migrated payment method", getCaseId(context));

        return caseData;
    }
}