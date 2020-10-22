package uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.DateFieldSetupTask;

@Component
@RequiredArgsConstructor
public class GeneralReferralDecisionDateTask extends DateFieldSetupTask {

    @Override
    protected String getFieldName() {
        return CcdFields.GENERAL_REFERRAL_DECISION_DATE;
    }
}
