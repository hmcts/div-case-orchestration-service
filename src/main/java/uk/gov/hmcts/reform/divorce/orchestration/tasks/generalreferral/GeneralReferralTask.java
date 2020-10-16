package uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.ExtractorHelper.getMandatoryStringValue;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;

@Component
@Slf4j
public class GeneralReferralTask implements Task<Map<String, Object>> {

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        String generalReferral = getMandatoryStringValue(caseData, CcdFields.GENERAL_REFERRAL_FEE);

        if (isRequiresPayment(generalReferral)) {
            log.info("CaseID: {} General Referral requires payment. Updating state to {}", getCaseId(context),
                CcdStates.AWAITING_GENERAL_REFERRAL_PAYMENT);
        } else {
            log.info("CaseID: {} General Referral does not require payment. Updating state to {}", getCaseId(context),
                CcdStates.AWAITING_GENERAL_CONSIDERATION);
        }
        return caseData;
    }

    private boolean isRequiresPayment(String mandatoryStringValue) {
        return YES_VALUE.equalsIgnoreCase(mandatoryStringValue);
    }
}
