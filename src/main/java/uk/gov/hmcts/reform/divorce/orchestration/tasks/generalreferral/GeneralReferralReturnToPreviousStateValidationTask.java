package uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeneralReferralReturnToPreviousStateValidationTask implements Task<Map<String, Object>> {

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) {
        String previousCaseState = getMandatoryPropertyValueAsString(payload, CcdFields.GENERAL_REFERRAL_PREVIOUS_CASE_STATE);

        final String caseId = getCaseId(context);

        log.info("CaseID: {} Previous case state retrieved: {}", caseId, previousCaseState);

        return payload;
    }
}
