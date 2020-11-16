package uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_STATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryContextValue;
import static uk.gov.hmcts.reform.divorce.orchestration.util.GeneralReferralHelper.isStatePartOfGeneralReferralWorkflow;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeneralReferralSetPreviousCaseStateTask implements Task<Map<String, Object>> {

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) {
        final String caseId = getCaseId(context);
        final String caseState = getMandatoryContextValue(CASE_STATE_JSON_KEY, context);

        if (isStatePartOfGeneralReferralWorkflow(caseState)) {
            log.info("CaseID: {} Case state {} is a General Referral workflow state. Previous case state not updated.", caseId, caseState);
            return payload;
        }

        log.info("CaseID: {} setting up previous case state to {}", caseId, caseState);

        payload.put(CcdFields.GENERAL_REFERRAL_PREVIOUS_CASE_STATE, caseState);

        return payload;
    }
}
