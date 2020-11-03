package uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;
import static uk.gov.hmcts.reform.divorce.orchestration.util.GeneralReferralHelper.isGeneralReferralRejected;

@Component
@RequiredArgsConstructor
@Slf4j
public class VerifyIsEligibleForStateRollbackBeforeGeneralReferralTask implements Task<Map<String, Object>> {

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) {
        if (!isGeneralReferralRejected(payload)) {
            throw new TaskException("Your previous general referral application has not been rejected. You cannot rollback the state.");
        }

        String previousCaseState = getMandatoryPropertyValueAsString(payload, CcdFields.GENERAL_REFERRAL_PREVIOUS_CASE_STATE);

        final String caseId = getCaseId(context);

        log.info("CaseID: {} Previous case state retrieved: {}", caseId, previousCaseState);

        return payload;
    }
}
